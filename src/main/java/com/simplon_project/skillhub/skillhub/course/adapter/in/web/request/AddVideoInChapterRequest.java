package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.AddVideoCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public record AddVideoInChapterRequest(
        @NotBlank
        String format,
        @Min(1) long durationInSeconds,
        int width,
        int height
) {
    public AddVideoInChapterRequest {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("Format must not be blank");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than 0");
        }

    }

    public AddVideoCommand toVideoCommand(String courseId, String sectionId, String chapterId, MultipartFile file) {
        return new AddVideoCommand(
                courseId,
                sectionId,
                chapterId,
                file,
                format,
                Duration.ofSeconds(durationInSeconds),
                width,
                height,
                file.getSize()
        );

    }
}
