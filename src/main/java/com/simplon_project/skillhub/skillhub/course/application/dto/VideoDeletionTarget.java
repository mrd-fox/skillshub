package com.simplon_project.skillhub.skillhub.course.application.dto;

import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;

import java.util.Objects;

/**
 * Minimal model required by deletion worker.
 * Avoids leaking JPA entities into application ports.
 */
public record VideoDeletionTarget(
        String videoId,
        String sourceUri,
        ExternalDeletionStatus externalDeletionStatus,
        Integer deleteAttemptCount
) {
    public VideoDeletionTarget {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        if (sourceUri == null || sourceUri.isBlank()) {
            throw new IllegalArgumentException("sourceUri must not be blank");
        }
        Objects.requireNonNull(externalDeletionStatus, "externalDeletionStatus must not be null");
    }
}