package com.simplon_project.skillhub.skillhub.course.application.dto;

import java.util.Objects;

public record ProviderPollingSnapshot(
        ProviderPollingStateEnum state,
        String providerVideoId,
        String thumbnailUrl,
        Long durationSeconds,
        Integer width,
        Integer height,
        Long sizeBytes,
        String format,
        String embedHash,
        String errorMessage
) {

    public ProviderPollingSnapshot {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(providerVideoId, "providerVideoId must not be null");
    }
}
