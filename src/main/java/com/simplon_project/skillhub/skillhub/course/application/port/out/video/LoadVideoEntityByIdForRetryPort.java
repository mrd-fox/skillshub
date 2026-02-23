package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionTarget;

import java.util.Optional;

/**
 * Output port for loading video entities including soft-deleted ones.
 * Used by retry operations that need to access FAILED deletion videos.
 */
public interface LoadVideoEntityByIdForRetryPort {

    /**
     * Load video including soft-deleted rows.
     * This bypasses the @SQLRestriction filter.
     *
     * @param videoId Video identifier as String
     * @return Optional containing video deletion target info, or empty if not found
     */
    Optional<VideoDeletionTarget> loadIncludingSoftDeleted(String videoId);
}

