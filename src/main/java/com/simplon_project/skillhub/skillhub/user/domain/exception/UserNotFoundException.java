package com.simplon_project.skillhub.skillhub.user.domain.exception;

import org.zalando.problem.Status;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String userId) {
        super("User not found with id: " + userId, Status.NOT_FOUND);
    }
}