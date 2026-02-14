package com.simplon_project.skillhub.skillhub.course.application.exception;

/**
 * Permanent (non-retryable) deletion failure.
 * Example: HTTP 4xx (except 404) due to client/auth/rate-limit/payload issues.
 */
public class VideoProviderDeletionPermanentException extends RuntimeException {

    public VideoProviderDeletionPermanentException(String message) {
        super(message);
    }

    public VideoProviderDeletionPermanentException(String message, Throwable cause) {
        super(message, cause);
    }
}