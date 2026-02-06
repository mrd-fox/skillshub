package com.simplon_project.skillhub.skillhub.course.domain.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class CourseAlreadySubmittedException extends AbstractThrowableProblem {

    public CourseAlreadySubmittedException(String courseId, String currentStatus) {
        super(null, "course-already-submitted", Status.CONFLICT,
                String.format("Course %s is already in status %s and cannot be submitted again", courseId, currentStatus));
    }
}
