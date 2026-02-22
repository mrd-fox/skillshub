package com.simplon_project.skillhub.skillhub.course.application.port.out;

import java.util.UUID;

/**
 * Output port for verifying if a user is enrolled in a specific course.
 * Used for enrollment-based access control.
 */
public interface IsUserEnrolledInCoursePort {

    /**
     * Check if a user is enrolled in a specific course.
     *
     * @param externalUserId the external user ID (Keycloak UUID)
     * @param courseId       the course UUID
     * @return true if enrolled, false otherwise
     */
    boolean isEnrolled(UUID externalUserId, UUID courseId);
}

