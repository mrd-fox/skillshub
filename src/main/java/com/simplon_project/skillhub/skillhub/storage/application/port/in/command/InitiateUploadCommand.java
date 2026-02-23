package com.simplon_project.skillhub.skillhub.storage.application.port.in.command;

import lombok.Builder;

public record InitiateUploadCommand(
        String uploaderId,
        String courseId,
        String chapterId,
        String filename,
        String contentType,
        long size
) {

    @Builder
    public InitiateUploadCommand(
            String uploaderId,
            String courseId,
            String chapterId,
            String filename,
            String contentType,
            long size

    ) {
        this.uploaderId = uploaderId;
        this.courseId = courseId;
        this.chapterId = chapterId;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }
}
