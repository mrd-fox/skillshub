package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record SearchCoursesByIdsCommand(
        List<Id> courseIds,
        UUID externalUserId
) {

    public SearchCoursesByIdsCommand {
        if (courseIds == null || courseIds.isEmpty()) {
            throw new IllegalArgumentException("Course IDs list cannot be null or empty");
        }

        courseIds.forEach(id -> Objects.requireNonNull(id, "Course ID cannot be null"));

        if (externalUserId == null) {
            throw new IllegalArgumentException("External user ID cannot be null");
        }
    }

    public static SearchCoursesByIdsCommand of(List<String> rawIds, String externalUserIdRaw) {
        if (rawIds == null || rawIds.isEmpty()) {
            throw new IllegalArgumentException("Course IDs list cannot be null or empty");
        }

        if (externalUserIdRaw == null || externalUserIdRaw.isBlank()) {
            throw new IllegalArgumentException("External user ID cannot be null or blank");
        }

        List<Id> validatedIds = rawIds.stream()
                .map(rawId -> {
                    if (rawId == null || rawId.isBlank()) {
                        throw new IllegalArgumentException("Course ID cannot be null or blank");
                    }

                    try {
                        UUID.fromString(rawId);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid UUID format for course ID: " + rawId);
                    }

                    return Id.of(rawId);
                })
                .toList();

        UUID externalUserId;
        try {
            externalUserId = UUID.fromString(externalUserIdRaw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for external user ID: " + externalUserIdRaw);
        }

        return new SearchCoursesByIdsCommand(validatedIds, externalUserId);
    }
}

