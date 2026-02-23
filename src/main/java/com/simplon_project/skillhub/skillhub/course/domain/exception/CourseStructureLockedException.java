package com.simplon_project.skillhub.skillhub.course.domain.exception;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exception thrown when a course structure update is attempted while videos are in-flight.
 * <p>
 * In-flight statuses (PENDING, PROCESSING) indicate that video processing is ongoing,
 * and structural changes (sections/chapters) must be blocked to prevent data inconsistency.
 */
public class CourseStructureLockedException extends AbstractThrowableProblem {

    private final Id courseId;
    private final Set<VideoStatusEnum> lockedStatuses;

    public CourseStructureLockedException(Id courseId, Set<VideoStatusEnum> lockedStatuses) {
        super(
                null,
                "course-structure-locked",
                Status.CONFLICT,
                String.format(
                        "Cannot update course structure for course %s: videos with in-flight status detected (%s). " +
                                "Please wait for video processing to complete before making structural changes.",
                        courseId.asString(),
                        lockedStatuses.stream()
                                .map(Enum::name)
                                .collect(Collectors.joining(", "))
                )
        );
        this.courseId = courseId;
        this.lockedStatuses = lockedStatuses;
    }

    public Id getCourseId() {
        return courseId;
    }

    public Set<VideoStatusEnum> getLockedStatuses() {
        return lockedStatuses;
    }
}

