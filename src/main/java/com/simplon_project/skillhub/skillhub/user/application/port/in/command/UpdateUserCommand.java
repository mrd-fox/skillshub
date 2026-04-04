package com.simplon_project.skillhub.skillhub.user.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * PATCH-like command.
 * Sanitization + validation happen here (not in use case).
 * - Strings are normalized: trim + blank -> null
 * - rolesToAdd normalized to uppercase
 * - rolesToAdd allowed values: only TUTOR (admin out of scope)
 */
public record UpdateUserCommand(
        String externalId,
        String firstName,
        String lastName,
        String address,
        String postalCode,
        String city,
        String country,
        String phoneNumber,
        Set<String> rolesToAdd
) {

    public UpdateUserCommand {
        externalId = normalizeRequired(externalId);

        firstName = normalizeOptional(firstName);
        lastName = normalizeOptional(lastName);
        address = normalizeOptional(address);
        postalCode = normalizeOptional(postalCode);
        city = normalizeOptional(city);
        country = normalizeOptional(country);
        phoneNumber = normalizeOptional(phoneNumber);

        rolesToAdd = normalizeRoles(rolesToAdd);

        validateNotEmpty(firstName, lastName, address, postalCode, city, country, phoneNumber, rolesToAdd);
        validateRolesAllowed(rolesToAdd);
    }

    private static String normalizeRequired(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException("externalId is required.");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return Helper.sanitize(trimmed);
    }

    private static Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        return roles.stream()
                .filter(r -> r != null && !r.isBlank())
                .map(r -> r.trim().toUpperCase())
                .collect(Collectors.toSet());
    }

    private static void validateNotEmpty(
            String firstName,
            String lastName,
            String address,
            String postalCode,
            String city,
            String country,
            String phoneNumber,
            Set<String> rolesToAdd
    ) {
        boolean hasProfileField =
                firstName != null
                        || lastName != null
                        || address != null
                        || postalCode != null
                        || city != null
                        || country != null
                        || phoneNumber != null;

        boolean hasRoles = rolesToAdd != null && !rolesToAdd.isEmpty();

        if (!hasProfileField && !hasRoles) {
            throw new IllegalArgumentException("UpdateUserCommand is empty: provide at least one field to update.");
        }
    }

    private static void validateRolesAllowed(Set<String> rolesToAdd) {
        if (rolesToAdd == null || rolesToAdd.isEmpty()) {
            return;
        }

        for (String role : rolesToAdd) {
            if (!"TUTOR".equals(role)) {
                throw new IllegalArgumentException("Only TUTOR role can be added for now.");
            }
        }
    }
}