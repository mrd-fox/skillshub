package com.simplon_project.skillhub.skillhub.course.adapter.messaging.listeners;

import com.simplon_project.skillhub.skillhub.course.adapter.messaging.VideoDeletionMessage;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionTarget;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderDeletionException;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderDeletionPermanentException;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.LoadVideoEntityByIdPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.SaveVideoEntityPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.VideoProviderDeletionPort;
import com.simplon_project.skillhub.skillhub.course.config.RabbitCourseVideoDeletionProps;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("VideoDeletionListener")
class VideoDeletionListenerTest {

    private LoadVideoEntityByIdPort loadPort;
    private SaveVideoEntityPort savePort;
    private VideoProviderDeletionPort providerPort;
    private RabbitTemplate rabbitTemplate;

    private RabbitCourseVideoDeletionProps deletionProps;

    private VideoDeletionListener listener;

    @BeforeEach
    void setup() {
        loadPort = mock(LoadVideoEntityByIdPort.class);
        savePort = mock(SaveVideoEntityPort.class);
        providerPort = mock(VideoProviderDeletionPort.class);
        rabbitTemplate = mock(RabbitTemplate.class);

        deletionProps = new RabbitCourseVideoDeletionProps();
        deletionProps.setEnabled(true);
        deletionProps.setDeletionDelayRoutingKey("course.video.deletion.delay");
        deletionProps.setMaxRetryAttempts(5);

        listener = new VideoDeletionListener(loadPort, savePort, providerPort, rabbitTemplate, deletionProps);

        // exchange is still read via @Value in the listener => set it for tests
        ReflectionTestUtils.setField(listener, "exchange", "course.events");
    }

    @Test
    @DisplayName("null message => ignore (ACK) and do nothing")
    void onMessage_nullMessage_shouldDoNothing() {
        listener.onMessage(null);

        verifyNoInteractions(loadPort, savePort, providerPort, rabbitTemplate);
    }

    @Test
    @DisplayName("video not found in DB => ACK and skip")
    void onMessage_videoNotFound_shouldAckAndSkip() {
        VideoDeletionMessage msg = new VideoDeletionMessage("vid-1", "vimeo://123", 1, Instant.now());
        when(loadPort.loadIncludingSoftDeleted("vid-1")).thenReturn(Optional.empty());

        listener.onMessage(msg);

        verify(loadPort).loadIncludingSoftDeleted("vid-1");
        verifyNoInteractions(savePort, providerPort, rabbitTemplate);
    }

    @Test
    @DisplayName("already DELETED => ACK and skip (idempotent)")
    void onMessage_alreadyDeleted_shouldAckAndSkip() {
        VideoDeletionMessage msg = new VideoDeletionMessage("vid-1", "vimeo://123", 1, Instant.now());
        VideoDeletionTarget target = new VideoDeletionTarget(
                "vid-1",
                "vimeo://123",
                ExternalDeletionStatus.DELETED,
                3
        );
        when(loadPort.loadIncludingSoftDeleted("vid-1")).thenReturn(Optional.of(target));

        listener.onMessage(msg);

        verify(loadPort).loadIncludingSoftDeleted("vid-1");
        verifyNoInteractions(savePort, providerPort, rabbitTemplate);
    }

    @Test
    @DisplayName("status != REQUESTED => ACK and skip")
    void onMessage_statusNotRequested_shouldAckAndSkip() {
        VideoDeletionMessage msg = new VideoDeletionMessage("vid-1", "vimeo://123", 1, Instant.now());
        VideoDeletionTarget target = new VideoDeletionTarget(
                "vid-1",
                "vimeo://123",
                ExternalDeletionStatus.NONE,
                0
        );
        when(loadPort.loadIncludingSoftDeleted("vid-1")).thenReturn(Optional.of(target));

        listener.onMessage(msg);

        verify(loadPort).loadIncludingSoftDeleted("vid-1");
        verifyNoInteractions(savePort, providerPort, rabbitTemplate);
    }

    @Test
    @DisplayName("attempt > maxRetryAttempts => markFailed and do not call provider")
    void onMessage_attemptExceedsMax_shouldFailAndStop() {
        deletionProps.setMaxRetryAttempts(3);

        VideoDeletionMessage msg = new VideoDeletionMessage("vid-1", "vimeo://123", 4, Instant.now());
        VideoDeletionTarget target = new VideoDeletionTarget(
                "vid-1",
                "vimeo://123",
                ExternalDeletionStatus.REQUESTED,
                0
        );
        when(loadPort.loadIncludingSoftDeleted("vid-1")).thenReturn(Optional.of(target));

        listener.onMessage(msg);

        verify(savePort).markFailed(eq("vid-1"), eq(4), contains("Max retry attempts exceeded"));
        verifyNoInteractions(providerPort, rabbitTemplate);
    }

    @Test
    @DisplayName("provider success => markDeleted")
    void onMessage_providerSuccess_shouldMarkDeleted() {
        VideoDeletionMessage msg = new VideoDeletionMessage("vid-1", "vimeo://123", 1, Instant.now());
        VideoDeletionTarget target = new VideoDeletionTarget(
                "vid-1",
                "vimeo://123",
                ExternalDeletionStatus.REQUESTED,
                0
        );
        when(loadPort.loadIncludingSoftDeleted("vid-1")).thenReturn(Optional.of(target));

        listener.onMessage(msg);

        verify(providerPort).delete("vimeo://123");
        verify(savePort).markDeleted("vid-1");
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("provider permanent failure => markFailed (no retry)")
    void onMessage_providerPermanentFailure_shouldMarkFailedNoRetry() {
        VideoDeletionMessage msg = new VideoDeletionMessage("vid-1", "vimeo://123", 2, Instant.now());
        VideoDeletionTarget target = new VideoDeletionTarget(
                "vid-1",
                "vimeo://123",
                ExternalDeletionStatus.REQUESTED,
                0
        );
        when(loadPort.loadIncludingSoftDeleted("vid-1")).thenReturn(Optional.of(target));

        doThrow(new VideoProviderDeletionPermanentException("401 unauthorized"))
                .when(providerPort).delete("vimeo://123");

        listener.onMessage(msg);

        verify(providerPort).delete("vimeo://123");
        verify(savePort).markFailed(eq("vid-1"), eq(2), contains("401"));
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("provider transient failure => markRetryScheduled + enqueue delay message with ttl")
    void onMessage_providerTransientFailure_shouldScheduleRetry() {
        deletionProps.setMaxRetryAttempts(5);

        VideoDeletionMessage msg = new VideoDeletionMessage("vid-1", "vimeo://123", 1, Instant.now());
        VideoDeletionTarget target = new VideoDeletionTarget(
                "vid-1",
                "vimeo://123",
                ExternalDeletionStatus.REQUESTED,
                0
        );
        when(loadPort.loadIncludingSoftDeleted("vid-1")).thenReturn(Optional.of(target));

        doThrow(new VideoProviderDeletionException("HTTP 503"))
                .when(providerPort).delete("vimeo://123");

        listener.onMessage(msg);

        verify(savePort).markRetryScheduled(eq("vid-1"), eq(2), contains("HTTP 503"));

        ArgumentCaptor<VideoDeletionMessage> msgCaptor = ArgumentCaptor.forClass(VideoDeletionMessage.class);
        ArgumentCaptor<MessagePostProcessor> mppCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        verify(rabbitTemplate).convertAndSend(
                eq("course.events"),
                eq("course.video.deletion.delay"),
                msgCaptor.capture(),
                mppCaptor.capture()
        );

        VideoDeletionMessage sent = msgCaptor.getValue();
        assertThat(sent.videoId()).isEqualTo("vid-1");
        assertThat(sent.sourceUri()).isEqualTo("vimeo://123");
        assertThat(sent.attempt()).isEqualTo(2);

        // attempt=2 => delay 10s
        MessageProperties props = new MessageProperties();
        Message springMsg = new Message(new byte[0], props);
        Message processed = mppCaptor.getValue().postProcessMessage(springMsg);

        assertThat(processed.getMessageProperties().getExpiration()).isEqualTo("10000");
    }

    @Test
    @DisplayName("transient failure but nextAttempt exceeds max => markFailed and do not enqueue")
    void onMessage_transientFailureButNextExceedsMax_shouldFailNoEnqueue() {
        deletionProps.setMaxRetryAttempts(2);

        VideoDeletionMessage msg = new VideoDeletionMessage("vid-1", "vimeo://123", 2, Instant.now());
        VideoDeletionTarget target = new VideoDeletionTarget(
                "vid-1",
                "vimeo://123",
                ExternalDeletionStatus.REQUESTED,
                0
        );
        when(loadPort.loadIncludingSoftDeleted("vid-1")).thenReturn(Optional.of(target));

        doThrow(new VideoProviderDeletionException("HTTP 503"))
                .when(providerPort).delete("vimeo://123");

        listener.onMessage(msg);

        verify(savePort).markFailed(eq("vid-1"), eq(2), contains("Max retry attempts exceeded"));
        verifyNoInteractions(rabbitTemplate);
    }
}