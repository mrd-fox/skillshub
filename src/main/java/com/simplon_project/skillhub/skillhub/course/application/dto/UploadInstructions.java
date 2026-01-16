package com.simplon_project.skillhub.skillhub.course.application.dto;

import java.time.Instant;
import java.util.Objects;

public record UploadInstructions(
        String provider,
        String uploadUrl,
        Instant expiresAt
) {
    public UploadInstructions {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(uploadUrl, "uploadUrl must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }
}
