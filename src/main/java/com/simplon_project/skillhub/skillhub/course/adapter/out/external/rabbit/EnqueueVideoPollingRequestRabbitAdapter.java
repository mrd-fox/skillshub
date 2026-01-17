package com.simplon_project.skillhub.skillhub.course.adapter.out.external.rabbit;

import com.simplon_project.skillhub.skillhub.course.application.port.out.video.EnqueueVideoPollingRequestPort;
import com.simplon_project.skillhub.skillhub.course.config.RabbitCourseVideoPollingProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnqueueVideoPollingRequestRabbitAdapter implements EnqueueVideoPollingRequestPort {
    private final RabbitTemplate courseRabbitTemplate;
    private final RabbitCourseVideoPollingProps pollingProps;

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

        Instant firstRequestedAt = Objects.requireNonNullElseGet(requestedAt, Instant::now);

        VideoPollingRequestedEvent payload = new VideoPollingRequestedEvent(
                videoId,
                attempt,
                firstRequestedAt.toString()
        );

        String routingKey;
        if (delayMs > 0) {
            routingKey = pollingProps.getPollingDelayRoutingKey();
        } else {
            routingKey = pollingProps.getPollingRoutingKey();
        }

        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalStateException("Polling routingKey is not configured");
        }

        if (delayMs > 0) {
            String expiration = Long.toString(delayMs);

            courseRabbitTemplate.convertAndSend(routingKey, payload, message -> {
                message.getMessageProperties().setExpiration(expiration);
                message.getMessageProperties().setContentType("application/json");
                message.getMessageProperties().setContentEncoding(StandardCharsets.UTF_8.name());
                message.getMessageProperties().setHeader("x-attempt", attempt);
                message.getMessageProperties().setHeader("x-first-requested-at", firstRequestedAt.toString());
                return message;
            });

            log.info("⏳ Enqueued video polling retry (delayed). videoId={} attempt={} delayMs={}", videoId, attempt, delayMs);
            return;
        }

        courseRabbitTemplate.convertAndSend(routingKey, payload, message -> {
            message.getMessageProperties().setContentType("application/json");
            message.getMessageProperties().setContentEncoding(StandardCharsets.UTF_8.name());
            message.getMessageProperties().setHeader("x-attempt", attempt);
            message.getMessageProperties().setHeader("x-first-requested-at", firstRequestedAt.toString());
            return message;
        });

        log.info("📨 Enqueued video polling (immediate). videoId={} attempt={}", videoId, attempt);
    }

    /**
     * Stable payload for polling orchestration.
     * Keep provider-agnostic: only videoId + retry context.
     */
    public record VideoPollingRequestedEvent(
            String videoId,
            int attempt,
            String firstRequestedAt
    ) {
        public VideoPollingRequestedEvent {
            if (videoId == null || videoId.isBlank()) {
                throw new IllegalArgumentException("videoId must not be blank");
            }
            if (attempt < 0) {
                throw new IllegalArgumentException("attempt must be >= 0");
            }
            if (firstRequestedAt == null || firstRequestedAt.isBlank()) {
                throw new IllegalArgumentException("firstRequestedAt must not be blank");
            }
        }
    }
}
