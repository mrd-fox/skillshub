package com.simplon_project.skillhub.skillhub.user.application.port.in.command;

import java.util.UUID;

/**
 * Command to enroll a user in a course.
 * Validation and parsing are performed at construction time.
 */
public record EnrollInCourseCommand(
        UUID externalUserId,
        UUID courseId,
        String rawRoles
) {

    public static EnrollInCourseCommand of(String externalUserId,
                                           String courseId,
                                           String rawRoles) {

        UUID externalUserIdUuid = parseUuid(externalUserId, "externalUserId");
        UUID courseIdUuid = parseUuid(courseId, "courseId");

        if (rawRoles == null || rawRoles.isBlank()) {
            throw new IllegalArgumentException("rawRoles cannot be null or blank");
        }

        return new EnrollInCourseCommand(externalUserIdUuid, courseIdUuid, rawRoles);
    }

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

    private static UUID parseUuid(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid UUID: " + value);
        }
    }
}