package com.simplon_project.skillhub.skillhub.common;

import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

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
        String sanitized = sanitize(value);
        if (sanitized == null) {
            return null;
        }
        String trimmed = sanitized.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return trimmed;
    }


    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = Jsoup.clean(value, Safelist.none());
        // If result is empty/blank after HTML removal, return null
        if (cleaned.isBlank()) {
            return null;
        }
        return cleaned;
    }


    public static String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }


}
