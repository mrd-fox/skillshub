package com.simplon_project.skillhub.skillhub.storage.domaine.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MediaContent {
    private final MediaId id;
    private final String filename;
    private final String contentType;
    private final long size;
    private final LocalDateTime createdAt;
    private final String url;

    @Builder
    public MediaContent(MediaId id, String filename, String contentType, long size, LocalDateTime createdAt, String url) {
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
        this.url = url;
    }

    public MediaContent withUrl(String url) {
        return new MediaContent(id, filename, contentType, size, createdAt, url);
    }
}
