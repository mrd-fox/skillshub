package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "PresignedUploadResponse")
public record PresignedUploadResponse(
        @Schema(description = "Media identifier (UUID).") String mediaId,
        @Schema(description = "Object storage key.") String storageKey,
        @Schema(description = "PreSigned URL to PUT the file.") String presignedUrl,
        @Schema(description = "Expiration time (UTC).", format = "date-time") Instant expiresAt
) {
}