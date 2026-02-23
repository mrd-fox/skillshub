package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.application.dto.ProviderPollingSnapshot;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderPollingException;

import java.util.Optional;

/**
 * Outbound port used by the application layer to poll an external video provider.
 *
 * <p>Rules respected:
 * <ul>
 *   <li>Provider-agnostic contract.</li>
 *   <li>No business semantics.</li>
 *   <li>One port = one responsibility.</li>
 * </ul>
 */
public interface VideoProviderPollingPort {
    /**
     * Poll the provider for the current state of the asset referenced by the canonical source URI.
     *
     * <p>Examples: vimeo://123456789
     *
     * @param sourceUri canonical URI stored in DB (non-null, non-blank)
     * @return empty if the provider cannot find the resource (invalid/deleted id)
     * @throws VideoProviderPollingException on transient failures (timeouts, 5xx, network)
     */
    Optional<ProviderPollingSnapshot> poll(String sourceUri);
}
