package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import lombok.Builder;

import java.util.Set;

/**
 * Command for deleting a course.
 * This command encapsulates the data needed to delete a course in the system.
 */
public record DeleteCourseCommand(
        String externalUserId,
        String rawRoles,
        String courseId
) {

    @Builder
    public DeleteCourseCommand {
        // ---- auth ----
        if (externalUserId == null || externalUserId.isBlank()) {
            throw new IllegalArgumentException("X-User-Id is missing or blank");
        }

        Set<UserRole> roles = Helper.extractUserRoles(rawRoles);
        if (!roles.contains(UserRole.TUTOR) && !roles.contains(UserRole.ADMIN)) {
            throw new IllegalArgumentException("Only TUTOR or ADMIN can delete a course");
        }

        // ---- path ----
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course ID must not be blank");
        }
    }
}

