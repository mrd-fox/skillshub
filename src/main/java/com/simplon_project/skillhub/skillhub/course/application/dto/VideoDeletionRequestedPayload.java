package com.simplon_project.skillhub.skillhub.course.application.dto;

/**
 * Payload for VIDEO_DELETION_REQUESTED outbox event.
 * Contains minimal information needed to process external video deletion asynchronously.
 * This payload is serialized to JSON and stored in the outbox_events table.
 */
public record VideoDeletionRequestedPayload(
        String videoId,
        String sourceUri
) {
    public VideoDeletionRequestedPayload {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        if (sourceUri == null || sourceUri.isBlank()) {
            throw new IllegalArgumentException("sourceUri must not be blank");
        }
    }
}
