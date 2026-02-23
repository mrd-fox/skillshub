package com.simplon_project.skillhub.skillhub.course.domain.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * Exception thrown when a student attempts to access a course they are not enrolled in.
 * Maps to HTTP 403 Forbidden.
 */
public class StudentNotEnrolledException extends AbstractThrowableProblem {

    public StudentNotEnrolledException(String courseId) {
        super(
                null,
                "student-not-enrolled",
                Status.FORBIDDEN,
                String.format("You must be enrolled in course %s to access it", courseId)
        );
    }
}

