package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import lombok.Builder;

import java.util.Set;

public record GetCoursesCommand(
        String externalAuthorId,
        Set<UserRole> userRoles
) {

    @Builder
    public static GetCoursesCommand of(String externalAuthorId, String rawRoles) {

        if (externalAuthorId == null || externalAuthorId.isBlank()) {
            throw new IllegalArgumentException("X-User-Id is missing or blank");
        }

        if (rawRoles == null || rawRoles.isBlank()) {
            throw new IllegalArgumentException("X-User-Roles is missing or blank");
        }

        Set<UserRole> roles = Helper.extractUserRoles(rawRoles);
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("X-User-Roles is missing or blank");
        }

        // ✅ appel du constructeur canonique
        return new GetCoursesCommand(externalAuthorId, roles);
    }
}
