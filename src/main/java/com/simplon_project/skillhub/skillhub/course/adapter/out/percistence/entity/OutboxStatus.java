package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

/**
 * Status of an outbox event in the transactional outbox pattern.
 * Stored as STRING in the database for better readability and maintainability.
 */
public enum OutboxStatus {
    /**
     * Event created but not yet sent to external system
     */
    PENDING,

    /**
     * Event successfully sent to external system
     */
    SENT,

    /**
     * Event failed after retry attempts, requires manual intervention
     */
    FAILED
}
