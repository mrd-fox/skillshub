package com.simplon_project.skillhub.skillhub.user.application.port.out;

import java.util.List;
import java.util.UUID;

/**
 * Output port for loading enrolled course IDs for a user.
 */
public interface LoadEnrolledCourseIdsPort {

    /**
     * Load all course IDs that a user is enrolled in.
     *
     * @param internalUserId the internal user UUID (user_account.id)
     * @return list of course UUIDs
     */
    List<UUID> loadCourseIds(UUID internalUserId);
}

