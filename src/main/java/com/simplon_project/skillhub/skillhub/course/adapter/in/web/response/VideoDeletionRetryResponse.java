package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for video deletion retry operation.
 */
@Schema(name = "VideoDeletionRetryResponse", description = "Result of a manual video deletion retry")
public record VideoDeletionRetryResponse(

        @Schema(description = "Video identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        String videoId,

        @Schema(description = "External deletion status after retry", example = "REQUESTED")
        String externalDeletionStatus,

        @Schema(description = "Number of deletion attempts (reset to 0 on retry)", example = "0")
        int attempts,

        @Schema(description = "Last error message (null after retry)", example = "null", nullable = true)
        String lastError
) {
}

