package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

public record VideoResponse(
        String id,
        String sourceUri,
        Long duration,
        String format,
        Long size,
        Integer width,
        Integer height,
        String thumbnailUrl,
        String errorMessage,
        String status
) {}
