package com.simplon_project.skillhub.skillhub.storage.domaine.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class NodeIdValidationException extends AbstractThrowableProblem {
    public NodeIdValidationException() {
        super(null, "invalid-id-value", Status.BAD_REQUEST, "Nod's id is invalid");
    }
}
