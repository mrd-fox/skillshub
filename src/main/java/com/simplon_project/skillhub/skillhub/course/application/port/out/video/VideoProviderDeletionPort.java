package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderDeletionException;

/**
 * Outbound port for deleting videos from external provider (e.g., Vimeo).
 *
 * <p>Rules respected:
 * <ul>
 *   <li>Provider-agnostic contract</li>
 *   <li>One port = one responsibility (video deletion)</li>
 *   <li>Must be called OUTSIDE JPA transaction</li>
 *   <li>Idempotent: HTTP 404 = SUCCESS</li>
 * </ul>
 */
public interface VideoProviderDeletionPort {

    /**
     * Delete the video from the external provider.
     *
     * <p>Expected behaviors:
     * <ul>
     *   <li>HTTP 204 (No Content) = SUCCESS</li>
     *   <li>HTTP 404 (Not Found) = SUCCESS (already deleted, idempotent)</li>
     *   <li>HTTP 5xx or network error = throw VideoProviderDeletionException for retry</li>
     * </ul>
     *
     * @param sourceUri canonical URI (e.g., vimeo://123456789)
     * @throws VideoProviderDeletionException on transient failures (5xx, timeouts, network)
     * @throws IllegalArgumentException       if sourceUri is null or blank
     */
    void delete(String sourceUri);
}
