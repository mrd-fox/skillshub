package com.simplon_project.skillhub.skillhub.course.application.port.out;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.Set;
import java.util.UUID;

/**
 * Port OUT for loading enrolled course IDs for a given user.
 * Returns course IDs from the enrollment domain.
 */
public interface LoadEnrolledCourseIdsPort {

    /**
     * Load all course IDs that a user is enrolled in.
     *
     * @param externalUserId the external user ID (Keycloak UUID)
     * @return a set of course domain IDs, empty set if no enrollments
     */
    Set<Id> loadEnrolledCourseIds(UUID externalUserId);
}

