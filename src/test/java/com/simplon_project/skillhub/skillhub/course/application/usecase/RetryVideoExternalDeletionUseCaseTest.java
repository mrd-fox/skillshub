package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionRetryResult;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionTarget;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.RetryVideoExternalDeletionCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.outbox.EnqueueOutboxEventPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.LoadVideoEntityByIdForRetryPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.ResetVideoExternalDeletionPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryVideoExternalDeletionUseCase Unit Tests")
class RetryVideoExternalDeletionUseCaseTest {

    private static final String VIDEO_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String SOURCE_URI = "vimeo://123456789";
    private static final String USER_ID = "admin-user-123";
    private static final Set<UserRole> ADMIN_ROLES = Set.of(UserRole.ADMIN);

    @Mock
    private LoadVideoEntityByIdForRetryPort loadVideoPort;

    @Mock
    private ResetVideoExternalDeletionPort resetDeletionPort;

    @Mock
    private EnqueueOutboxEventPort enqueueOutboxEventPort;

    @InjectMocks
    private RetryVideoExternalDeletionUseCase useCase;

    private RetryVideoExternalDeletionCommand createValidCommand() {
        return RetryVideoExternalDeletionCommand.builder()
                .videoId(VIDEO_ID)
                .externalUserId(USER_ID)
                .roles(ADMIN_ROLES)
                .build();
    }

    private VideoDeletionTarget createFailedVideo() {
        return new VideoDeletionTarget(
                VIDEO_ID,
                SOURCE_URI,
                ExternalDeletionStatus.FAILED,
                3 // had 3 failed attempts
        );
    }

    @Nested
    @DisplayName("retry() method")
    class RetryMethod {

        @Test
        @DisplayName("should successfully retry when video is in FAILED status")
        void retry_shouldSucceedWhenFailed() {
            // GIVEN
            var command = createValidCommand();
            var failedVideo = createFailedVideo();

            when(loadVideoPort.loadIncludingSoftDeleted(VIDEO_ID))
                    .thenReturn(Optional.of(failedVideo));

            // WHEN
            VideoDeletionRetryResult result = useCase.retry(command);

            // THEN
            assertNotNull(result);
            assertEquals(VIDEO_ID, result.videoId());
            assertEquals(ExternalDeletionStatus.REQUESTED, result.externalDeletionStatus());
            assertEquals(0, result.attempts());
            assertNull(result.lastError());

            // Verify reset was called
            verify(resetDeletionPort).resetToRequested(VIDEO_ID);

            // Verify outbox event was enqueued
            ArgumentCaptor<Id> idCaptor = ArgumentCaptor.forClass(Id.class);
            verify(enqueueOutboxEventPort).enqueueVideoDeletionRequested(
                    idCaptor.capture(),
                    eq(SOURCE_URI)
            );
            assertEquals(VIDEO_ID, idCaptor.getValue().asString());
        }

        @Test
        @DisplayName("should fail when video is not found")
        void retry_shouldFailWhenVideoNotFound() {
            // GIVEN
            var command = createValidCommand();
            when(loadVideoPort.loadIncludingSoftDeleted(VIDEO_ID))
                    .thenReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.retry(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Video not found")
                    .hasMessageContaining(VIDEO_ID);

            verify(resetDeletionPort, never()).resetToRequested(any());
            verify(enqueueOutboxEventPort, never()).enqueueVideoDeletionRequested(any(), any());
        }

        @Test
        @DisplayName("should fail when video is in REQUESTED status")
        void retry_shouldFailWhenStatusRequested() {
            // GIVEN
            var command = createValidCommand();
            var requestedVideo = new VideoDeletionTarget(
                    VIDEO_ID,
                    SOURCE_URI,
                    ExternalDeletionStatus.REQUESTED,
                    1
            );

            when(loadVideoPort.loadIncludingSoftDeleted(VIDEO_ID))
                    .thenReturn(Optional.of(requestedVideo));

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.retry(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot retry deletion")
                    .hasMessageContaining("REQUESTED")
                    .hasMessageContaining("expected FAILED");

            verify(resetDeletionPort, never()).resetToRequested(any());
            verify(enqueueOutboxEventPort, never()).enqueueVideoDeletionRequested(any(), any());
        }

        @Test
        @DisplayName("should fail when video is in DELETED status")
        void retry_shouldFailWhenStatusDeleted() {
            // GIVEN
            var command = createValidCommand();
            var deletedVideo = new VideoDeletionTarget(
                    VIDEO_ID,
                    SOURCE_URI,
                    ExternalDeletionStatus.DELETED,
                    2
            );

            when(loadVideoPort.loadIncludingSoftDeleted(VIDEO_ID))
                    .thenReturn(Optional.of(deletedVideo));

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.retry(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot retry deletion")
                    .hasMessageContaining("DELETED")
                    .hasMessageContaining("expected FAILED");

            verify(resetDeletionPort, never()).resetToRequested(any());
            verify(enqueueOutboxEventPort, never()).enqueueVideoDeletionRequested(any(), any());
        }

        @Test
        @DisplayName("should fail when video is in NONE status")
        void retry_shouldFailWhenStatusNone() {
            // GIVEN
            var command = createValidCommand();
            var noneVideo = new VideoDeletionTarget(
                    VIDEO_ID,
                    SOURCE_URI,
                    ExternalDeletionStatus.NONE,
                    0
            );

            when(loadVideoPort.loadIncludingSoftDeleted(VIDEO_ID))
                    .thenReturn(Optional.of(noneVideo));

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.retry(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot retry deletion")
                    .hasMessageContaining("NONE")
                    .hasMessageContaining("expected FAILED");

            verify(resetDeletionPort, never()).resetToRequested(any());
            verify(enqueueOutboxEventPort, never()).enqueueVideoDeletionRequested(any(), any());
        }

        @Test
        @DisplayName("should fail when command is null")
        void retry_shouldFailWhenCommandNull() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.retry(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("command must not be null");

            verify(loadVideoPort, never()).loadIncludingSoftDeleted(any());
            verify(resetDeletionPort, never()).resetToRequested(any());
            verify(enqueueOutboxEventPort, never()).enqueueVideoDeletionRequested(any(), any());
        }

        @Test
        @DisplayName("should enqueue outbox event exactly once on success")
        void retry_shouldEnqueueOutboxEventOnce() {
            // GIVEN
            var command = createValidCommand();
            var failedVideo = createFailedVideo();

            when(loadVideoPort.loadIncludingSoftDeleted(VIDEO_ID))
                    .thenReturn(Optional.of(failedVideo));

            // WHEN
            useCase.retry(command);

            // THEN
            verify(enqueueOutboxEventPort, times(1))
                    .enqueueVideoDeletionRequested(any(Id.class), eq(SOURCE_URI));
        }
    }

    @Nested
    @DisplayName("Command validation")
    class CommandValidation {

        @Test
        @DisplayName("should reject command without ADMIN role")
        void command_shouldRejectNonAdmin() {
            // WHEN / THEN
            assertThatThrownBy(() ->
                    RetryVideoExternalDeletionCommand.builder()
                            .videoId(VIDEO_ID)
                            .externalUserId(USER_ID)
                            .roles(Set.of(UserRole.TUTOR))
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Only ADMIN can retry video deletions");
        }

        @Test
        @DisplayName("should reject command with blank videoId")
        void command_shouldRejectBlankVideoId() {
            // WHEN / THEN
            assertThatThrownBy(() ->
                    RetryVideoExternalDeletionCommand.builder()
                            .videoId("   ")
                            .externalUserId(USER_ID)
                            .roles(ADMIN_ROLES)
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("videoId must not be blank");
        }

        @Test
        @DisplayName("should reject command with blank externalUserId")
        void command_shouldRejectBlankUserId() {
            // WHEN / THEN
            assertThatThrownBy(() ->
                    RetryVideoExternalDeletionCommand.builder()
                            .videoId(VIDEO_ID)
                            .externalUserId("   ")
                            .roles(ADMIN_ROLES)
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("X-User-Id is missing or blank");
        }

        @Test
        @DisplayName("should reject command with empty roles")
        void command_shouldRejectEmptyRoles() {
            // WHEN / THEN
            assertThatThrownBy(() ->
                    RetryVideoExternalDeletionCommand.builder()
                            .videoId(VIDEO_ID)
                            .externalUserId(USER_ID)
                            .roles(Set.of())
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("X-User-Roles is missing or empty");
        }
    }
}


