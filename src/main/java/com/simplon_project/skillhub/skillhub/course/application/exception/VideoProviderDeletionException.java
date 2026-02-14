package com.simplon_project.skillhub.skillhub.course.application.exception;

/**
 * Exception thrown when video provider deletion fails due to transient errors.
 * Indicates that retry should be attempted.
 *
 * <p>Use for:
 * <ul>
 *   <li>HTTP 5xx errors from provider</li>
 *   <li>Network timeouts</li>
 *   <li>Connection failures</li>
 * </ul>
 *
 * <p>Do NOT use for:
 * <ul>
 *   <li>HTTP 404 (already deleted = success)</li>
 *   <li>HTTP 204 (success)</li>
 *   <li>Authentication errors (permanent failures)</li>
 * </ul>
 */
public class VideoProviderDeletionException extends RuntimeException {

    public VideoProviderDeletionException(String message) {
        super(message);
    }

    public VideoProviderDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
