package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.exception.MissingUserContextException;
import lombok.Builder;

import java.util.Set;

/**
 * Command to publish a course (submit it for validation).
 */
@Builder
public record PublishCourseCommand(
        String courseId,
        String externalUserId,
        Set<UserRole> userRoles
) {

    /**
     * Factory method with validation.
     */
    public static PublishCourseCommand of(String courseId, String externalUserId, String rawRoles) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId is required");
        }

        if (externalUserId == null || externalUserId.isBlank()) {
            throw new MissingUserContextException("X-User-Id header is required");
        }

        if (rawRoles == null || rawRoles.isBlank()) {
            throw new MissingUserContextException("X-User-Roles header is required");
        }

        Set<UserRole> roles = Helper.extractUserRoles(rawRoles);
        if (roles.isEmpty()) {
            throw new MissingUserContextException("X-User-Roles header contains no valid roles");
        }

        return new PublishCourseCommand(courseId, externalUserId, roles);
    }
}
