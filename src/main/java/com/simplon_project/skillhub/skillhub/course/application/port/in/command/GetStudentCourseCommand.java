package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.exception.MissingUserContextException;

import java.util.Set;
import java.util.UUID;

/**
 * Command for retrieving a course as a student.
 * Includes validation at construction time.
 */
public record GetStudentCourseCommand(
        String courseId,
        String externalUserId,
        Set<UserRole> userRoles
) {

    public static GetStudentCourseCommand of(
            String courseId,
            String externalUserId,
            String userRolesCsv
    ) {
        // Validate required fields
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course ID must not be blank");
        }

        if (externalUserId == null || externalUserId.isBlank()) {
            throw new MissingUserContextException("X-User-Id header is required");
        }

        if (userRolesCsv == null || userRolesCsv.isBlank()) {
            throw new MissingUserContextException("X-User-Roles header is required");
        }

        // Validate UUID format
        try {
            UUID.fromString(courseId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid courseId format: must be a valid UUID");
        }

        try {
            UUID.fromString(externalUserId);
        } catch (IllegalArgumentException e) {
            throw new MissingUserContextException("Invalid externalUserId format: must be a valid UUID");
        }

        // Parse roles
        Set<UserRole> roles = Helper.extractUserRoles(userRolesCsv);

        return new GetStudentCourseCommand(courseId, externalUserId, roles);
    }
}

