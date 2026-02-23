package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.Builder;

import java.util.Set;

public record GetCourseCommand(
        String externalAuthorId,
        Set<UserRole> userRoles,
        String courseId
) {

    @Builder
    public static GetCourseCommand of(String externalAuthorId, String rawRoles, String courseId) {

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
        return new GetCourseCommand(externalAuthorId, roles, courseId);
    }

    public Course mapTodomain() {
        return Course.builder().id(Id.of(this.courseId)).build();
    }

}
