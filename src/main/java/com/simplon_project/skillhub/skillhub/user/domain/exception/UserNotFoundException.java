package com.simplon_project.skillhub.skillhub.user.domain.exception;

import org.zalando.problem.Status;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String userId, String message) {
        super(message + " " + userId, Status.NOT_FOUND);
    }
}