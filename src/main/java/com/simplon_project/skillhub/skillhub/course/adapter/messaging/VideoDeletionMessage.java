package com.simplon_project.skillhub.skillhub.course.adapter.messaging;

import java.time.Instant;
import java.util.Objects;

/**
 * Rabbit message for video deletion worker.
 * Carries minimal information to process external video deletion.
 *
 * <p>Technical DTO. No business logic. Stable payload for retries and delay queues.
 */
public record VideoDeletionMessage(
        String videoId,
        String sourceUri,
        int attempt,
        Instant enqueuedAt
) {
    public VideoDeletionMessage {
        Objects.requireNonNull(videoId, "videoId must not be null");
        Objects.requireNonNull(sourceUri, "sourceUri must not be null");
        Objects.requireNonNull(enqueuedAt, "enqueuedAt must not be null");

        if (videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        if (sourceUri.isBlank()) {
            throw new IllegalArgumentException("sourceUri must not be blank");
        }
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must be >= 0");
        }
    }

    /**
     * Create first attempt message from outbox payload.
     */
    public static VideoDeletionMessage firstAttempt(String videoId, String sourceUri) {
        return new VideoDeletionMessage(videoId, sourceUri, 1, Instant.now());
    }

    /**
     * Create next retry attempt.
     */
    public VideoDeletionMessage nextAttempt() {
        return new VideoDeletionMessage(
                this.videoId,
                this.sourceUri,
                this.attempt + 1,
                Instant.now()
        );
    }
}
