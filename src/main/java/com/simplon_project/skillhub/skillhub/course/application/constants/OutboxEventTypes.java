package com.simplon_project.skillhub.skillhub.course.application.constants;

/**
 * Constants for outbox event types and aggregate types.
 * Used for transactional outbox pattern to ensure reliable async processing.
 */
public final class OutboxEventTypes {

    private OutboxEventTypes() {
        // Utility class, no instantiation
    }

    // Event Types
    public static final String VIDEO_DELETION_REQUESTED = "VIDEO_DELETION_REQUESTED";

    // Aggregate Types
    public static final String AGGREGATE_TYPE_VIDEO = "VIDEO";
}
