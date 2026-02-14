package com.simplon_project.skillhub.skillhub.course.application.dto;

import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;

/**
 * Result DTO for video deletion retry operation.
 * Contains current state after retry attempt.
 */
public record VideoDeletionRetryResult(
        String videoId,
        ExternalDeletionStatus externalDeletionStatus,
        int attempts,
        String lastError
) {
}

