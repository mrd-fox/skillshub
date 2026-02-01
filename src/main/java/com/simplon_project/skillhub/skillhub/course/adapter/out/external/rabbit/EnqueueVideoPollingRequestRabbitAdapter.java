package com.simplon_project.skillhub.skillhub.course.adapter.out.external.rabbit;

import com.simplon_project.skillhub.skillhub.course.adapter.messaging.VideoPollingMessage;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.EnqueueVideoPollingRequestPort;
import com.simplon_project.skillhub.skillhub.course.config.RabbitCourseVideoPollingProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@Component
public class EnqueueVideoPollingRequestRabbitAdapter implements EnqueueVideoPollingRequestPort {
    private final RabbitTemplate courseRabbitTemplate;
    private final RabbitCourseVideoPollingProps pollingProps;

    @Value("${course.rabbitmq.exchange}")
    private String exchange;

    public EnqueueVideoPollingRequestRabbitAdapter(
            @Qualifier("courseRabbitTemplate") RabbitTemplate courseRabbitTemplate,
            RabbitCourseVideoPollingProps pollingProps) {
        this.courseRabbitTemplate = courseRabbitTemplate;
        this.pollingProps = pollingProps;
    }

    @Override
    public void enqueue(String videoId, int attempt, Instant requestedAt, long delayMs) {

        if (!pollingProps.isEnabled()) {
            log.debug("Video polling disabled. Skip enqueue. videoId={} attempt={}", videoId, attempt);
            return;
        }

        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must be >= 0");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs must be >= 0");
        }
        if (exchange == null || exchange.isBlank()) {
            throw new IllegalStateException("Rabbit exchange is not configured (course.rabbitmq.exchange)");
        }

        Instant enqueuedAt = Objects.requireNonNullElseGet(requestedAt, Instant::now);

        VideoPollingMessage payload = new VideoPollingMessage(
                videoId,
                attempt,
                enqueuedAt,
                null, // size
                null, // duration
                null, // width
                null, // height
                null, // thumbnailUrl
                null  // embedHash
        );

        String routingKey = (delayMs > 0)
                ? pollingProps.getPollingDelayRoutingKey()
                : pollingProps.getPollingRoutingKey();

        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalStateException("Polling routingKey is not configured");
        }

        if (delayMs > 0) {
            String expiration = Long.toString(delayMs);

            courseRabbitTemplate.convertAndSend(exchange, routingKey, payload, message -> {
                message.getMessageProperties().setExpiration(expiration);
                // Ensure message is treated as JSON and UTF-8 for reliable deserialization when retried
                message.getMessageProperties().setContentType("application/json");
                message.getMessageProperties().setContentEncoding(StandardCharsets.UTF_8.name());
                return message;
            });

            log.info("⏳ Enqueued video polling (delayed). videoId={} attempt={} delayMs={}", videoId, attempt, delayMs);
            return;
        }

        courseRabbitTemplate.convertAndSend(exchange, routingKey, payload, message -> {
            // Consistency: set content type and encoding even for immediate messages
            message.getMessageProperties().setContentType("application/json");
            message.getMessageProperties().setContentEncoding(StandardCharsets.UTF_8.name());
            return message;
        });

        log.info("📨 Enqueued video polling (immediate). videoId={} attempt={}", videoId, attempt);
    }
}
