package com.simplon_project.skillhub.skillhub.course.adapter.messaging.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.messaging.VideoDeletionMessage;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxEventEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxStatus;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaOutboxEventRepository;
import com.simplon_project.skillhub.skillhub.course.application.constants.OutboxEventTypes;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionRequestedPayload;
import com.simplon_project.skillhub.skillhub.course.config.RabbitCourseVideoDeletionProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service for processing individual outbox events.
 * Separated from dispatcher to ensure proper transaction boundaries (avoid self-invocation).
 *
 * <p>Each event is processed in its own transaction to isolate failures.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "course.rabbitmq.video-deletion", name = "enabled", havingValue = "true")
public class OutboxEventProcessor {

    private final JpaOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitCourseVideoDeletionProps deletionProps;

    @Value("${course.rabbitmq.exchange}")
    private String exchange;

    public OutboxEventProcessor(
            JpaOutboxEventRepository outboxRepository,
            @Qualifier("courseRabbitTemplate") RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            RabbitCourseVideoDeletionProps deletionProps
    ) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.deletionProps = deletionProps;
    }

    /**
     * Process a single outbox event in its own transaction:
     * - Deserialize payload
     * - Publish to RabbitMQ
     * - Update status (SENT / FAILED) and retryCount/lastError
     */
    @Transactional("courseTxManager")
    public void processEvent(OutboxEventEntity event) {

        String outboxId = event.getId() != null && event.getId().value() != null
                ? event.getId().value().toString()
                : "unknown";

        // Defensive: only PENDING events are eligible
        if (event.getStatus() != OutboxStatus.PENDING) {
            log.debug("Outbox event already processed, skipping: outboxId={} status={}", outboxId, event.getStatus());
            return;
        }

        // Only VIDEO_DELETION_REQUESTED is supported
        if (!OutboxEventTypes.VIDEO_DELETION_REQUESTED.equals(event.getEventType())) {
            int retry = event.getRetryCount() != null ? event.getRetryCount() : 0;
            log.warn(
                    "Outbox processor does not support eventType={}, marking FAILED: outboxId={}",
                    event.getEventType(),
                    outboxId
            );
            markFailed(event, retry, "Unsupported event type: " + event.getEventType());
            return;
        }

        try {
            // 1) Deserialize payload
            VideoDeletionRequestedPayload payload = objectMapper.readValue(
                    event.getPayload(),
                    VideoDeletionRequestedPayload.class
            );

            // 2) Build RabbitMQ message (first attempt for provider)
            VideoDeletionMessage message = VideoDeletionMessage.firstAttempt(
                    payload.videoId(),
                    payload.sourceUri()
            );

            // 3) Publish to RabbitMQ (main deletion routing key)
            rabbitTemplate.convertAndSend(
                    exchange,
                    deletionProps.getDeletionRoutingKey(),
                    message
            );

            // 4) Mark event as SENT
            event.setStatus(OutboxStatus.SENT);
            event.setSentAt(Instant.now());
            event.setLastError(null);
            outboxRepository.save(event);

            log.info(
                    "Outbox event published successfully: outboxId={} videoId={} eventType={}",
                    outboxId,
                    payload.videoId(),
                    event.getEventType()
            );

        } catch (Exception ex) {

            int currentRetry = event.getRetryCount() != null ? event.getRetryCount() : 0;
            int nextRetry = currentRetry + 1;

            String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();

            if (nextRetry >= deletionProps.getMaxRetryAttempts()) {

                // IMPORTANT: persist final retry count for operational correctness
                markFailed(event, nextRetry, "Max retries exceeded (" + nextRetry + "): " + errorMsg);

                log.error(
                        "Outbox event FAILED after {} retries: outboxId={} eventType={} error={}",
                        nextRetry,
                        outboxId,
                        event.getEventType(),
                        errorMsg,
                        ex
                );

            } else {

                // Retry later → increment retry count, keep PENDING
                event.setRetryCount(nextRetry);
                event.setLastError(errorMsg);
                outboxRepository.save(event);

                log.warn(
                        "Outbox event publish failed, will retry: outboxId={} eventType={} retry={}/{} error={}",
                        outboxId,
                        event.getEventType(),
                        nextRetry,
                        deletionProps.getMaxRetryAttempts(),
                        errorMsg
                );
            }
        }
    }

    private void markFailed(OutboxEventEntity event, int retryCount, String errorMessage) {
        event.setStatus(OutboxStatus.FAILED);
        event.setRetryCount(retryCount);
        event.setLastError(errorMessage);
        outboxRepository.save(event);
    }
}