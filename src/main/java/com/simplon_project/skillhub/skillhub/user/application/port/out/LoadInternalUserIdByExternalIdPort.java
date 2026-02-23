package com.simplon_project.skillhub.skillhub.user.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for loading internal user ID from external user ID.
 */
public interface LoadInternalUserIdByExternalIdPort {

    /**
     * Load the internal user ID by external user ID.
     *
     * @param externalUserId the external user UUID (e.g., Keycloak ID)
     * @return Optional containing the internal user ID if found, empty otherwise
     */
    Optional<UUID> loadInternalUserId(UUID externalUserId);
}

