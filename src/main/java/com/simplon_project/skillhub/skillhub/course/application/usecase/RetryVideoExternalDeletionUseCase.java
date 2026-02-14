package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionRetryResult;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionTarget;
import com.simplon_project.skillhub.skillhub.course.application.port.in.RetryVideoExternalDeletionPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.RetryVideoExternalDeletionCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.outbox.EnqueueOutboxEventPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.LoadVideoEntityByIdForRetryPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.ResetVideoExternalDeletionPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for manually retrying FAILED external video deletions.
 * This is an admin operation to recover from permanent deletion failures.
 * <p>
 * Flow:
 * 1. Load video (including soft-deleted)
 * 2. Validate status is FAILED
 * 3. Reset status to REQUESTED and clear counters
 * 4. Enqueue new outbox event VIDEO_DELETION_REQUESTED
 * 5. Return current state
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryVideoExternalDeletionUseCase implements RetryVideoExternalDeletionPort {

    private final LoadVideoEntityByIdForRetryPort loadVideoPort;
    private final ResetVideoExternalDeletionPort resetDeletionPort;
    private final EnqueueOutboxEventPort enqueueOutboxEventPort;

    @Override
    @Transactional("courseTxManager")
    public VideoDeletionRetryResult retry(RetryVideoExternalDeletionCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        String videoId = command.videoId();

        log.info("Manual retry requested for video deletion: videoId={} by user={}",
                videoId, command.externalUserId());

        // Load video including soft-deleted
        VideoDeletionTarget video = loadVideoPort.loadIncludingSoftDeleted(videoId)
                .orElseThrow(() -> new IllegalStateException(
                        "Video not found (including soft-deleted): " + videoId
                ));

        // Validate status is FAILED
        if (video.externalDeletionStatus() != ExternalDeletionStatus.FAILED) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot retry deletion: video %s is in status %s (expected FAILED)",
                            videoId,
                            video.externalDeletionStatus()
                    )
            );
        }

        // Reset status to REQUESTED and clear counters
        resetDeletionPort.resetToRequested(videoId);

        // Enqueue new outbox event (transactional outbox pattern)
        enqueueOutboxEventPort.enqueueVideoDeletionRequested(
                Id.of(videoId),
                video.sourceUri()
        );

        log.info("Video deletion retry enqueued: videoId={} sourceUri={}",
                videoId, video.sourceUri());

        // Return result with reset state
        return new VideoDeletionRetryResult(
                videoId,
                ExternalDeletionStatus.REQUESTED,
                0, // attempts reset to 0
                null // error cleared
        );
    }
}

