package com.simplon_project.skillhub.skillhub.course.domain.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class MissingUserContextException extends AbstractThrowableProblem {

    public MissingUserContextException(String message) {
        super(null, "missing-user-context", Status.UNAUTHORIZED, message);
    }
}
