package com.simplon_project.skillhub.skillhub.course.domain.enums;

/**
 * Tracks the external deletion state for videos hosted on external platforms (e.g., Vimeo).
 * This is independent from the video upload/processing status (VideoStatusEnum).
 * Used for implementing Option B deletion strategy with reliable async external cleanup.
 */
public enum ExternalDeletionStatus {
    /**
     * No external deletion has been requested. Video exists on external platform.
     */
    NONE,

    /**
     * Deletion has been requested and is pending external confirmation.
     * Outbox event should be created/processed.
     */
    REQUESTED,

    /**
     * External deletion confirmed successful. Video no longer exists on external platform.
     */
    DELETED,

    /**
     * External deletion failed after retry attempts. Manual intervention may be required.
     */
    FAILED
}
