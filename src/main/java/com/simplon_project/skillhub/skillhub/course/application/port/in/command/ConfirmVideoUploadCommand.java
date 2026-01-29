package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

public record ConfirmVideoUploadCommand(
        String courseId,
        String sectionId,
        String chapterId

) {

    public ConfirmVideoUploadCommand {
        requireNotBlank(courseId, "courseId");
        requireNotBlank(sectionId, "sectionId");
        requireNotBlank(chapterId, "chapterId");


    }

    // -----------------
    // Private helpers
    // -----------------

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }


}
