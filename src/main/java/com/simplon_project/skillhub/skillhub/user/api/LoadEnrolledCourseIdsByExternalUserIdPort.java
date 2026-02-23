package com.simplon_project.skillhub.skillhub.user.api;

import java.util.Set;
import java.util.UUID;

/**
 * Internal module API for loading enrolled course IDs by external user ID.
 * <p>
 * This interface is exported as part of the user module's public API
 * for inter-module communication within the monolith.
 * <p>
 * It is intentionally placed in the 'api' package to clearly signal
 * that it is designed for consumption by other internal modules
 * (e.g., course module for enrollment entitlement checks).
 * <p>
 * This is NOT an HTTP API - it is for direct module-to-module calls.
 */
public interface LoadEnrolledCourseIdsByExternalUserIdPort {

    /**
     * Load all course IDs that a user is enrolled in.
     *
     * @param externalUserId the external user ID (Keycloak UUID)
     * @return a set of course UUIDs, empty set if no enrollments
     * @throws com.simplon_project.skillhub.skillhub.user.domain.exception.UserNotFoundException if the user does not exist
     */
    Set<UUID> loadEnrolledCourseIds(UUID externalUserId);
}

