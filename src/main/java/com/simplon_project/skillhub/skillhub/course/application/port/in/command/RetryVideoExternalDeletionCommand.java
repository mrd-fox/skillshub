package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import lombok.Builder;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command to manually retry a FAILED external video deletion.
 * Only ADMIN can retry deletions.
 */
public record RetryVideoExternalDeletionCommand(
        String videoId,
        String externalUserId,
        Set<UserRole> roles
) {

    @Builder
    public RetryVideoExternalDeletionCommand {
        // Validate videoId
        videoId = Helper.normalizeOptional(videoId);
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }

        // Validate authentication context
        if (externalUserId == null || externalUserId.isBlank()) {
            throw new IllegalArgumentException("X-User-Id is missing or blank");
        }

        // Parse roles
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("X-User-Roles is missing or empty");
        }

        // Enforce ADMIN role
        if (!roles.contains(UserRole.ADMIN)) {
            throw new IllegalArgumentException("Only ADMIN can retry video deletions");
        }
    }

    /**
     * Factory method to create command from controller parameters.
     */
    public static RetryVideoExternalDeletionCommand of(
            String videoId,
            String externalUserId,
            String rawRoles
    ) {
        if (rawRoles == null || rawRoles.isBlank()) {
            throw new IllegalArgumentException("X-User-Roles is missing or blank");
        }

        Set<UserRole> roles = parseRoles(rawRoles);

        return RetryVideoExternalDeletionCommand.builder()
                .videoId(videoId)
                .externalUserId(externalUserId)
                .roles(roles)
                .build();
    }

    private static Set<UserRole> parseRoles(String rawRoles) {
        return Arrays.stream(rawRoles.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(UserRole::valueOf)
                .collect(Collectors.toUnmodifiableSet());
    }
}


