package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.InitiateUploadCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "PresignedUploadRequest")
public record PresignedUploadRequest(
        @NotBlank @Schema(example = "big-buck-bunny-1080p.mp4") String filename,
        @NotBlank @Schema(example = "video/mp4") String contentType,
        @Min(1) @Schema(example = "73400320") long size,
        @NotBlank @Schema(example = "5c8f...") String courseId,
        @NotBlank @Schema(example = "1a2b...") String chapterId
) {

    public InitiateUploadCommand mapToCommand(String uploaderId) {
        return InitiateUploadCommand.builder()
                .uploaderId(uploaderId)
                .filename(filename)
                .contentType(contentType)
                .size(size)
                .courseId(courseId)
                .chapterId(chapterId)
                .build();
    }
}