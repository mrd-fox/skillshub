package com.simplon_project.skillhub.skillhub.course.application.dto;

public enum ProviderPollingStateEnum {
    /**
     * The asset exists but is still being processed (transcoding, encoding).
     */
    PROCESSING,

    /**
     * The asset exists and playback is available.
     */
    AVAILABLE,

    /**
     * The provider reports a terminal error for this asset.
     */
    ERROR
}
