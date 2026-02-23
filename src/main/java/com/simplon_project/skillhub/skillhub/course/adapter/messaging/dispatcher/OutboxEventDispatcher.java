package com.simplon_project.skillhub.skillhub.course.adapter.messaging.dispatcher;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxEventEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxStatus;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaOutboxEventRepository;
import com.simplon_project.skillhub.skillhub.course.config.RabbitCourseVideoDeletionProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outbox event dispatcher for video deletion.
 * Polls PENDING outbox events and delegates processing to OutboxEventProcessor.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Query PENDING outbox events (batch size configurable)</li>
 *   <li>Delegate to processor for transactional handling</li>
 *   <li>Isolate transaction boundaries (each event in own TX)</li>
 * </ul>
 *
 * <p>Enabled only when: course.rabbitmq.video-deletion.enabled=true
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "course.rabbitmq.video-deletion", name = "enabled", havingValue = "true")
public class OutboxEventDispatcher {

    private final JpaOutboxEventRepository outboxRepository;
    private final OutboxEventProcessor processor;
    private final RabbitCourseVideoDeletionProps deletionProps;

    /**
     * Scheduled dispatcher that polls PENDING outbox events and processes them.
     * Runs at fixed delay (configurable via properties).
     */
    @Scheduled(fixedDelayString = "${course.rabbitmq.video-deletion.dispatcher-fixed-delay-ms:5000}")
    public void dispatchPendingEvents() {

        int batchSize = deletionProps.getDispatcherBatchSize();

        List<OutboxEventEntity> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                PageRequest.of(0, batchSize)
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Outbox dispatcher found {} PENDING event(s)", pendingEvents.size());

        for (OutboxEventEntity event : pendingEvents) {
            try {
                processor.processEvent(event);
            } catch (Exception ex) {
                log.error(
                        "Unexpected error processing outbox event, will retry later: outboxId={} error={}",
                        event.getId() != null && event.getId().value() != null ? event.getId().value() : "unknown",
                        ex.getMessage(),
                        ex
                );
            }
        }
    }
}
