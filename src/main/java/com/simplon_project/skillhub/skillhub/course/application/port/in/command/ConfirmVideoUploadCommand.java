package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

public record ConfirmVideoUploadCommand(
        String courseId,
        String sectionId,
        String chapterId,
        String sourceUri
) {

    public ConfirmVideoUploadCommand {
        requireNotBlank(courseId, "courseId");
        requireNotBlank(sectionId, "sectionId");
        requireNotBlank(chapterId, "chapterId");
        requireNotBlank(sourceUri, "sourceUri");

        validateSourceUri(sourceUri);
    }

    // -----------------
    // Private helpers
    // -----------------

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private static void validateSourceUri(String sourceUri) {
        // agnostic validation: scheme://identifier
        int schemeSeparatorIndex = sourceUri.indexOf("://");

        if (schemeSeparatorIndex <= 0) {
            throw new IllegalArgumentException(
                    "sourceUri must be a valid URI with scheme (e.g. scheme://identifier)"
            );
        }

        String scheme = sourceUri.substring(0, schemeSeparatorIndex);

        if (scheme.isBlank()) {
            throw new IllegalArgumentException("sourceUri scheme must not be blank");
        }
    }
}
