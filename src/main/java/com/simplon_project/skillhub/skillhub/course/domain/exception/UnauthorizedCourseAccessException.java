package com.simplon_project.skillhub.skillhub.course.domain.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class UnauthorizedCourseAccessException extends AbstractThrowableProblem {

    public UnauthorizedCourseAccessException(String message) {
        super(null, "unauthorized-course-access", Status.FORBIDDEN, message);
    }
}
