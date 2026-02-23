package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;


public record InitVideoCommand(
        @NotBlank String courseId,
        @NotBlank String sectionId,
        @NotBlank String chapterId,
        @Positive long sizeBytes
) {

    public InitVideoCommand {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId is required");
        }
        if (sectionId == null || sectionId.isBlank()) {
            throw new IllegalArgumentException("sectionId is required");
        }
        if (chapterId == null || chapterId.isBlank()) {
            throw new IllegalArgumentException("chapterId is required");
        }
        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("sizeBytes must be > 0");
        }
    }
}