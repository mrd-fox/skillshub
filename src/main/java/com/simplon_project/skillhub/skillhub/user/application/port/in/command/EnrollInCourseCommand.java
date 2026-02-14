package com.simplon_project.skillhub.skillhub.user.application.port.in.command;

import java.util.UUID;

/**
 * Command to enroll a user in a course.
 * Immutable value object.
 * Validation is performed at construction time via the static factory method.
 */
public record EnrollInCourseCommand(
        UUID externalUserId,
        UUID courseId,
        String rawRoles
) {

    /**
     * Safe factory that validates inputs at build time.
     *
     * @param externalUserId external user UUID (Keycloak id)
     * @param courseId       course UUID
     * @param rawRoles       roles as CSV string
     * @return validated command instance
     */
    public static EnrollInCourseCommand of(UUID externalUserId,
                                           UUID courseId,
                                           String rawRoles) {

        if (externalUserId == null) {
            throw new IllegalArgumentException("externalUserId cannot be null");
        }

        if (courseId == null) {
            throw new IllegalArgumentException("courseId cannot be null");
        }

        if (rawRoles == null || rawRoles.isBlank()) {
            throw new IllegalArgumentException("rawRoles cannot be null or blank");
        }

        return new EnrollInCourseCommand(externalUserId, courseId, rawRoles);
    }

    /**
     * Check if the user has STUDENT role.
     * Unknown roles are ignored.
     *
     * @return true if STUDENT role is present, false otherwise
     */
    public boolean hasStudentRole() {
        String[] parts = rawRoles.split(",");
        for (String part : parts) {
            String normalized = part.trim().toUpperCase();
            if ("STUDENT".equals(normalized)) {
                return true;
            }
        }
        return false;
    }
}