package com.simplon_project.skillhub.skillhub.user.domain.exception;

import org.zalando.problem.Status;

/**
 * Exception thrown when a user lacks the required role to perform an action.
 */
public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super(message, Status.FORBIDDEN);
    }
}

