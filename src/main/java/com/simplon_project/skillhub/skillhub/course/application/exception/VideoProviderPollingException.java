package com.simplon_project.skillhub.skillhub.course.application.exception;

public class VideoProviderPollingException extends RuntimeException {

    public VideoProviderPollingException(String message) {
        super(message);
    }

    public VideoProviderPollingException(String message, Throwable cause) {
        super(message, cause);
    }
}
