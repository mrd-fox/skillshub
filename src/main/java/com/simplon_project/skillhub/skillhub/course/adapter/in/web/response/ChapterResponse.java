package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChapterResponse(
        String id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        VideoResponse video
) {
}
