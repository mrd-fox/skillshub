package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionTarget;

import java.util.Optional;

/**
 * Outbound port to load a video deletion target including soft-deleted rows.
 * Must bypass soft-delete restrictions.
 */
public interface LoadVideoEntityByIdPort {

    Optional<VideoDeletionTarget> loadIncludingSoftDeleted(String videoId);
}