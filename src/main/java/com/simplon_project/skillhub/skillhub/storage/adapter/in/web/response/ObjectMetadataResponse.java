package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(name = "ObjectMetadataResponse", description = "Object storage metadata")
public record ObjectMetadataResponse(
        @Schema(description = "Total size in bytes", example = "908930329") long sizeBytes,
        @Schema(description = "Entity tag (ETag), may include quotes", example = "\"2500e6c97221ed03d6c4204addcb9df2\"") String eTag,
        @Schema(description = "MIME type if set", example = "video/mp4") String contentType,
        @Schema(description = "Last modification timestamp (UTC)") Instant lastModified,
        @Schema(description = "User-defined metadata (x-amz-meta-*)") Map<String, String> userMetadata
) {
}
