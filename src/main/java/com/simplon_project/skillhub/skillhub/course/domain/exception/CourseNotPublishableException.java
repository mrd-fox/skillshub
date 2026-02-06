package com.simplon_project.skillhub.skillhub.course.domain.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class CourseNotPublishableException extends AbstractThrowableProblem {

    public CourseNotPublishableException(String reason) {
        super(null, "course-not-publishable", Status.UNPROCESSABLE_ENTITY,
                String.format("Course cannot be published: %s", reason));
    }
}
