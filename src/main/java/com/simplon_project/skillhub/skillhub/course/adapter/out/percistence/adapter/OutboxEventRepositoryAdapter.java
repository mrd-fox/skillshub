package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxEventEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxStatus;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaOutboxEventRepository;
import com.simplon_project.skillhub.skillhub.course.application.constants.OutboxEventTypes;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionRequestedPayload;
import com.simplon_project.skillhub.skillhub.course.application.port.out.outbox.EnqueueOutboxEventPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Adapter for persisting outbox events in the transactional outbox pattern.
 * Events are created in the same transaction as the business operation to ensure consistency.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional("courseTxManager")
public class OutboxEventRepositoryAdapter implements EnqueueOutboxEventPort {

    private final JpaOutboxEventRepository jpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void enqueueVideoDeletionRequested(Id videoId, String sourceUri) {
        if (videoId == null) {
            throw new IllegalArgumentException("videoId must not be null");
        }
        if (sourceUri == null || sourceUri.isBlank()) {
            throw new IllegalArgumentException("sourceUri must not be blank");
        }

        var payload = new VideoDeletionRequestedPayload(
                videoId.asString(),
                sourceUri
        );

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize VideoDeletionRequestedPayload to JSON", e);
        }

        var outboxEvent = OutboxEventEntity.builder()
                .id(EntityId.random())
                .eventType(OutboxEventTypes.VIDEO_DELETION_REQUESTED)
                .aggregateType(OutboxEventTypes.AGGREGATE_TYPE_VIDEO)
                .aggregateId(videoId.asUUID())
                .payload(payloadJson)
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .retryCount(0)
                .build();

        jpaRepository.save(outboxEvent);

        log.debug("Enqueued outbox event for video deletion: videoId={}, sourceUri={}",
                videoId.asString(), sourceUri);
    }
}
