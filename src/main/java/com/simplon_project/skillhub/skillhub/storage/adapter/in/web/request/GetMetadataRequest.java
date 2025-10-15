package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.GetObjectMetadataCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GetMetadataRequest", description = "Request to fetch object metadata by object key")
public record GetMetadataRequest(
        @Schema(description = "Object key inside the configured bucket", example = "videos/courses/.../file.mp4", required = true)
        String objectKey
) {
    public GetObjectMetadataCommand mapToCommand() {
        // Validate eagerly at build-time
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("objectKey must not be blank");
        }
        return new GetObjectMetadataCommand(objectKey);
    }
}
