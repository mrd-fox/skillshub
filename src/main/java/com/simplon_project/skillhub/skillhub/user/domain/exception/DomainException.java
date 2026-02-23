package com.simplon_project.skillhub.skillhub.user.domain.exception;

import lombok.Getter;
import org.zalando.problem.Status;

@Getter
public abstract class DomainException extends RuntimeException {

    private final Status status;

    protected DomainException(String message, Status status) {
        super(message);
        this.status = status;
    }

}
