package com.simplon_project.skillhub.skillhub.course.application.port.out.outbox;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

/**
 * Port for enqueueing outbox events in the transactional outbox pattern.
 * Events are persisted in the same transaction as the business operation.
 */
public interface EnqueueOutboxEventPort {

    /**
     * Enqueues a video deletion request event.
     * This creates an outbox event with status PENDING that will be processed asynchronously.
     *
     * @param videoId   Domain identifier of the video to delete
     * @param sourceUri Provider-specific URI (e.g., vimeo://12345)
     */
    void enqueueVideoDeletionRequested(Id videoId, String sourceUri);
}
