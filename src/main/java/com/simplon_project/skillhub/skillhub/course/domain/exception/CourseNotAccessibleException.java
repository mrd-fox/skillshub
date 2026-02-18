package com.simplon_project.skillhub.skillhub.course.domain.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * Exception thrown when a course is not accessible (e.g., not PUBLISHED).
 * Maps to HTTP 404 Not Found to avoid leaking course existence.
 */
public class CourseNotAccessibleException extends AbstractThrowableProblem {

    public CourseNotAccessibleException(String courseId) {
        super(
                null,
                "course-not-accessible",
                Status.NOT_FOUND,
                String.format("Course %s is not accessible", courseId)
        );
    }
}

