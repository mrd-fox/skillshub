package com.simplon_project.skillhub.skillhub.course.adapter.messaging;

import java.time.Instant;
import java.util.Objects;

/**
 * Rabbit message used to orchestrate provider polling for a video.
 *
 * <p>Technical DTO. No business logic. Stable payload for retries and delay queues.
 */
public record VideoPollingMessage(
        String videoId,
        int attempt,
        Instant enqueuedAt
) {

    public VideoPollingMessage {
        Objects.requireNonNull(videoId, "videoId must not be null");
        Objects.requireNonNull(enqueuedAt, "enqueuedAt must not be null");

        if (videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must be >= 0");
        }
    }

    public static VideoPollingMessage firstAttempt(String videoId) {
        return new VideoPollingMessage(videoId, 0, Instant.now());
    }

    public VideoPollingMessage nextAttempt() {
        return new VideoPollingMessage(this.videoId, this.attempt + 1, Instant.now());
    }
}