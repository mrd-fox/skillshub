package com.simplon_project.skillhub.skillhub.storage.adapter.out.storageresponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record PresignedUploadStroageResponse(
        @Schema(description = "Media identifier (UUID).") String mediaId,
        @Schema(description = "Object storage key.") String storageKey,
        @Schema(description = "PreSigned URL to PUT the file.") String presignedUrl,
        @Schema(description = "Expiration time (UTC).", format = "date-time") Instant expiresAt

) {

}
