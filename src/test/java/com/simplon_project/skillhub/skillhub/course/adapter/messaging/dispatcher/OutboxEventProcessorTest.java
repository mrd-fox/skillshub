package com.simplon_project.skillhub.skillhub.course.adapter.messaging.dispatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.messaging.VideoDeletionMessage;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxEventEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxStatus;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaOutboxEventRepository;
import com.simplon_project.skillhub.skillhub.course.application.constants.OutboxEventTypes;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionRequestedPayload;
import com.simplon_project.skillhub.skillhub.course.config.RabbitCourseVideoDeletionProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("OutboxEventProcessor")
class OutboxEventProcessorTest {

    private JpaOutboxEventRepository outboxRepository;
    private RabbitTemplate rabbitTemplate;
    private ObjectMapper objectMapper;
    private RabbitCourseVideoDeletionProps deletionProps;

    private OutboxEventProcessor processor;


    @BeforeEach
    void setup() {
        outboxRepository = mock(JpaOutboxEventRepository.class);
        rabbitTemplate = mock(RabbitTemplate.class);
        objectMapper = new ObjectMapper();
        deletionProps = new RabbitCourseVideoDeletionProps();

        deletionProps.setEnabled(true);
        deletionProps.setDeletionRoutingKey("course.video.deletion.requested");
        deletionProps.setDispatcherBatchSize(10);
        deletionProps.setMaxRetryAttempts(3);

        processor = new OutboxEventProcessor(
                outboxRepository,
                rabbitTemplate,
                objectMapper,
                deletionProps
        );

        ReflectionTestUtils.setField(processor, "exchange", "course.events");
    }

    @Test
    @DisplayName("processEvent - success => mark SENT, publish to Rabbit")
    void processEvent_success_shouldMarkSent() throws JsonProcessingException {
        String videoId = UUID.randomUUID().toString();
        String sourceUri = "vimeo://123456";

        VideoDeletionRequestedPayload payload = new VideoDeletionRequestedPayload(videoId, sourceUri);
        String payloadJson = objectMapper.writeValueAsString(payload);

        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(EntityId.random())
                .eventType(OutboxEventTypes.VIDEO_DELETION_REQUESTED)
                .aggregateType(OutboxEventTypes.AGGREGATE_TYPE_VIDEO)
                .aggregateId(UUID.fromString(videoId))
                .payload(payloadJson)
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .retryCount(0)
                .build();

        processor.processEvent(event);

        // Verify Rabbit publish
        ArgumentCaptor<VideoDeletionMessage> msgCaptor = ArgumentCaptor.forClass(VideoDeletionMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq("course.events"),
                eq("course.video.deletion.requested"),
                msgCaptor.capture()
        );

        VideoDeletionMessage sentMessage = msgCaptor.getValue();
        assertThat(sentMessage.videoId()).isEqualTo(videoId);
        assertThat(sentMessage.sourceUri()).isEqualTo(sourceUri);
        assertThat(sentMessage.attempt()).isEqualTo(1);

        // Verify event marked SENT
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(event.getSentAt()).isNotNull();
        assertThat(event.getLastError()).isNull();

        verify(outboxRepository).save(event);
    }

    @Test
    @DisplayName("processEvent - already SENT => skip")
    void processEvent_alreadySent_shouldSkip() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(EntityId.random())
                .eventType(OutboxEventTypes.VIDEO_DELETION_REQUESTED)
                .status(OutboxStatus.SENT)
                .createdAt(Instant.now())
                .build();

        processor.processEvent(event);

        verifyNoInteractions(rabbitTemplate);
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("processEvent - unsupported eventType => mark FAILED")
    void processEvent_unsupportedEventType_shouldMarkFailed() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(EntityId.random())
                .eventType("UNKNOWN_EVENT")
                .aggregateType("VIDEO")
                .payload("{}")
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .retryCount(0)
                .build();

        processor.processEvent(event);

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getLastError()).contains("Unsupported event type");

        verify(outboxRepository).save(event);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("processEvent - JSON parse error => mark FAILED after max retries")
    void processEvent_jsonParseError_shouldMarkFailed() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(EntityId.random())
                .eventType(OutboxEventTypes.VIDEO_DELETION_REQUESTED)
                .aggregateType(OutboxEventTypes.AGGREGATE_TYPE_VIDEO)
                .payload("INVALID JSON")
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .retryCount(2) // Already retried twice
                .build();

        processor.processEvent(event);

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getLastError()).contains("Max retries exceeded");

        verify(outboxRepository).save(event);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("processEvent - Rabbit publish exception => increment retry count")
    void processEvent_rabbitException_shouldIncrementRetry() throws JsonProcessingException {
        String videoId = UUID.randomUUID().toString();
        String sourceUri = "vimeo://123456";

        VideoDeletionRequestedPayload payload = new VideoDeletionRequestedPayload(videoId, sourceUri);
        String payloadJson = objectMapper.writeValueAsString(payload);

        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(EntityId.random())
                .eventType(OutboxEventTypes.VIDEO_DELETION_REQUESTED)
                .aggregateType(OutboxEventTypes.AGGREGATE_TYPE_VIDEO)
                .aggregateId(UUID.fromString(videoId))
                .payload(payloadJson)
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .retryCount(0)
                .build();

        doThrow(new RuntimeException("Rabbit connection error"))
                .when(rabbitTemplate).convertAndSend(any(), any(), any(VideoDeletionMessage.class));

        processor.processEvent(event);

        // Should increment retry count and stay PENDING
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getLastError()).contains("Rabbit connection error");

        verify(outboxRepository).save(event);
    }

    @Test
    @DisplayName("processEvent - Rabbit exception + max retries exceeded => mark FAILED")
    void processEvent_rabbitExceptionMaxRetries_shouldMarkFailed() throws JsonProcessingException {
        String videoId = UUID.randomUUID().toString();
        String sourceUri = "vimeo://123456";

        VideoDeletionRequestedPayload payload = new VideoDeletionRequestedPayload(videoId, sourceUri);
        String payloadJson = objectMapper.writeValueAsString(payload);

        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(EntityId.random())
                .eventType(OutboxEventTypes.VIDEO_DELETION_REQUESTED)
                .aggregateType(OutboxEventTypes.AGGREGATE_TYPE_VIDEO)
                .aggregateId(UUID.fromString(videoId))
                .payload(payloadJson)
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .retryCount(2) // Already retried twice
                .build();

        doThrow(new RuntimeException("Rabbit connection error"))
                .when(rabbitTemplate).convertAndSend(any(), any(), any(VideoDeletionMessage.class));

        processor.processEvent(event);

        // Should mark FAILED
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(3);
        assertThat(event.getLastError()).contains("Max retries exceeded");

        verify(outboxRepository).save(event);
    }
}
