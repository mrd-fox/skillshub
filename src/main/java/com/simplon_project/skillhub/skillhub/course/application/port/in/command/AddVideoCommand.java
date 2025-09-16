package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;


public record AddVideoCommand(
        @NotBlank String courseId,
        @NotBlank String sectionId,
        @NotBlank String chapterId,
        @NotNull MultipartFile file,
        @NotBlank String format,
        @NotNull Duration duration,
        @Positive int width,
        @Positive int height,
        @Min(1) long size
) {
    public AddVideoCommand {
        // auto-validation lors de l’instanciation
        if (courseId == null || courseId.isBlank()) throw new IllegalArgumentException("courseId is required");
        if (sectionId == null || sectionId.isBlank()) throw new IllegalArgumentException("sectionId is required");
        if (chapterId == null || chapterId.isBlank()) throw new IllegalArgumentException("chapterId is required");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("video file is required");
        if (width < 1280 || height < 720) throw new IllegalArgumentException("Minimum video resolution is 1280x720");
        if (duration.toSeconds() < 10) throw new IllegalArgumentException("Video must be at least 10 seconds long");
        if (!Set.of("mp4", "webm", "mov").contains(format.toLowerCase()))
            throw new IllegalArgumentException("Unsupported video format: " + format);
        if (size > 500 * 1024 * 1024) throw new IllegalArgumentException("Video size must be less than 500MB");

    }

    private static String generateStorageKey(String courseId, String sectionId, String chapterId) {
        return String.format("videos/%s/%s/%s/%s.mp4", courseId, sectionId, chapterId, UUID.randomUUID());
    }

    public VideoInfo mapToDomain() {
        return new VideoInfo(
                Id.random(),
                generateStorageKey(courseId, sectionId, chapterId), duration,
                format.toLowerCase(),
                size,
                width,
                height,
                VideoStatusEnum.PENDING
        );
    }
}
