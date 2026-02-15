package com.simplon_project.skillhub.skillhub.course.adapter.out.internal.user;

import com.simplon_project.skillhub.skillhub.course.application.port.out.LoadEnrolledCourseIdsPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.api.LoadEnrolledCourseIdsByExternalUserIdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Internal adapter for loading enrolled course IDs from user module.
 * Direct module-to-module communication (no HTTP).
 */
@Component
@RequiredArgsConstructor
public class UserModuleEnrollmentAdapter implements LoadEnrolledCourseIdsPort {

    private final LoadEnrolledCourseIdsByExternalUserIdPort userPort;

    @Override
    public Set<Id> loadEnrolledCourseIds(UUID externalUserId) {
        // Call user module to get enrolled course IDs
        Set<UUID> courseUuids = userPort.loadEnrolledCourseIds(externalUserId);

        // Map UUID to course domain Id (never null)
        if (courseUuids == null || courseUuids.isEmpty()) {
            return Set.of();
        }

        return courseUuids.stream()
                .map(uuid -> Id.of(uuid.toString()))
                .collect(Collectors.toSet());
    }
}

