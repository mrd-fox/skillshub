package com.simplon_project.skillhub.skillhub.course.adapter.out.internal.user;

import com.simplon_project.skillhub.skillhub.course.application.port.out.IsUserEnrolledInCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.LoadEnrolledCourseIdsPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.api.LoadEnrolledCourseIdsByExternalUserIdPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Internal adapter for loading enrolled course IDs from user module.
 * Direct module-to-module communication (no HTTP).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserModuleEnrollmentAdapter implements LoadEnrolledCourseIdsPort, IsUserEnrolledInCoursePort {

    private final LoadEnrolledCourseIdsByExternalUserIdPort userPort;

    @Override
    public Set<Id> loadEnrolledCourseIds(UUID externalUserId) {
        var courseUuids = userPort.loadEnrolledCourseIds(externalUserId);

        if (courseUuids == null || courseUuids.isEmpty()) {
            return Set.of();
        }

        return courseUuids.stream()
                .map(uuid -> Id.of(uuid.toString()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isEnrolled(UUID externalUserId, UUID courseId) {
        try {
            log.debug("Checking enrollment for user {} in course {}", externalUserId, courseId);

            var enrolledCourseIds = userPort.loadEnrolledCourseIds(externalUserId);
            var enrolled = enrolledCourseIds != null && enrolledCourseIds.contains(courseId);

            log.debug("User {} is {} enrolled in course {}",
                    externalUserId, enrolled ? "" : "NOT", courseId);

            return enrolled;
        } catch (Exception e) {
            log.error("Error checking enrollment for user {} in course {}: {}",
                    externalUserId, courseId, e.getMessage());
            return false;
        }
    }
}

