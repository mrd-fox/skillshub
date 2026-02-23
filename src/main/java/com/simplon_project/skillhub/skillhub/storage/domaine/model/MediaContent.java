package com.simplon_project.skillhub.skillhub.storage.domaine.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MediaContent {
    private final MediaId id;
    private final String uploaderId;
    private final String courseId;
    private final String chapterId;

    private final String filename;
    private final String contentType;
    private final long size;
    private final LocalDateTime createdAt;
    private final String url;

    @Builder
    public MediaContent(MediaId id,
                        String uploaderId,
                        String courseId,
                        String chapterId,
                        String filename,
                        String contentType,
                        long size,
                        LocalDateTime createdAt,
                        String url) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.courseId = courseId;
        this.chapterId = chapterId;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
        this.url = url;
    }

    public MediaContent withUrl(String url) {
        return new MediaContent(
                id, uploaderId, courseId, chapterId,
                filename, contentType, size, createdAt, url
        );
    }
}
