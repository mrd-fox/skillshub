package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.Set;

/**
 * Port for checking if a course contains any video with in-flight status.
 * <p>
 * In-flight statuses indicate video processing is ongoing (e.g., PENDING, PROCESSING).
 * This check is used to enforce structure lock during video operations.
 */
public interface ExistsInFlightVideoForCoursePort {

    /**
     * Check if the course contains at least one non-soft-deleted video with status in the provided set.
     *
     * @param courseId the course ID
     * @param statuses the set of video statuses to check (e.g., PENDING, PROCESSING)
     * @return true if at least one video exists with matching status, false otherwise
     */
    boolean existsInFlightVideoForCourse(Id courseId, Set<VideoStatusEnum> statuses);
}

