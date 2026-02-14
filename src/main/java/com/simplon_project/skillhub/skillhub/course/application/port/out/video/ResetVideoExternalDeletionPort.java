package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

/**
 * Output port for resetting video deletion state during retry operations.
 * This allows manual intervention to retry FAILED deletions.
 */
public interface ResetVideoExternalDeletionPort {

    /**
     * Reset video deletion state to REQUESTED and clear counters.
     * Used when manually retrying a FAILED deletion.
     *
     * @param videoId Video identifier as String
     */
    void resetToRequested(String videoId);
}

