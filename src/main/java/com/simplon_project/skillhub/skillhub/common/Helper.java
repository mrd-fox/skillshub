package com.simplon_project.skillhub.skillhub.common;

import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class Helper {
    private Helper() {
        // prevent instantiation
    }

    public static Set<UserRole> extractUserRoles(String rolesCsv) {
        if (rolesCsv == null || rolesCsv.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
    }

    public static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }


}
