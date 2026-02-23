package com.simplon_project.skillhub.skillhub.course.adapter.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class MissingArgumentException extends AbstractThrowableProblem {
    public MissingArgumentException(String argumentMissing) {
        super(null, "missing-required-argument", Status.BAD_REQUEST, argumentMissing);
    }
}
