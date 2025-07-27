package com.simplon_project.skillhub.skillhub.course.adapter.common.exception;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class CourseNotFoundException extends AbstractThrowableProblem {
    public CourseNotFoundException(Id courseId) {
        super(null, "course-not-found", Status.BAD_REQUEST, "course with id %s not found", courseId);
    }
}
