package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.CompleteUploadCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CommitUploadRequest")
public record CommitUploadRequest(
        @NotBlank @Schema(description = "Object storage key returned at request-upload") String storageKey,
        @NotBlank @Schema(example = "big-buck-bunny-1080p.mp4") String filename,
        @NotBlank @Schema(example = "5c8f...") String courseId,
        @NotBlank @Schema(example = "1a2b...") String chapterId
) {

    public CompleteUploadCommand mapToCommand(String uploaderId, String mediaId) {

        return CompleteUploadCommand.builder()
                .uploaderId(uploaderId)
                .courseId(courseId)
                .chapterId(chapterId)
                .mediaId(mediaId)
                .storageKey(storageKey)
                .filename(filename)
                .build();
    }
}
