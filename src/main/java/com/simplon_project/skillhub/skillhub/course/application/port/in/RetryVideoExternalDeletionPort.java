package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionRetryResult;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.RetryVideoExternalDeletionCommand;

/**
 * Input port for manually retrying FAILED video external deletions.
 * This operation resets the deletion state and re-enqueues an outbox event.
 */
public interface RetryVideoExternalDeletionPort {

    /**
     * Retry external deletion for a video that is in FAILED state.
     *
     * @param command Command containing videoId and authenticated admin context
     * @return Result containing current deletion status
     * @throws IllegalArgumentException if video is not in FAILED state
     * @throws IllegalStateException    if video not found
     */
    VideoDeletionRetryResult retry(RetryVideoExternalDeletionCommand command);
}

