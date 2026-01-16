package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import java.util.Objects;

public record InitProviderUploadCommand(
        long sizeBytes,
        String title,
        String description,
        String privacy // "private" | "unlisted" | "public" (provider-agnostic)
) {
    public InitProviderUploadCommand {
        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("sizeBytes must be > 0");
        }
        Objects.requireNonNull(privacy, "privacy must not be null");
    }
}
