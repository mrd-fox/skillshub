package com.simplon_project.skillhub.skillhub.course.domain.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class CourseAlreadyExistsException extends AbstractThrowableProblem {

    public CourseAlreadyExistsException(String title) {
        super(null, "course-already-exists", Status.BAD_REQUEST, String.format("Course entity with title %s already exists", title));
    }
}
