package com.simplon_project.skillhub.skillhub.storage.application.port.in.command;

import lombok.Builder;

public record CompleteUploadCommand(
        String uploaderId,
        String courseId,
        String chapterId,
        String mediaId,
        String storageKey,
        String filename
) {

    @Builder
    public CompleteUploadCommand(
            String uploaderId,
            String courseId,
            String chapterId,
            String mediaId,
            String storageKey,
            String filename

    ) {
        this.uploaderId = uploaderId;
        this.courseId = courseId;
        this.chapterId = chapterId;
        this.mediaId = mediaId;
        this.storageKey = storageKey;
        this.filename = filename;
    }
}
