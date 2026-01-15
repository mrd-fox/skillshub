package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SectionResponse(
        String id,
        String title,
        Integer position,
        List<ChapterResponse> chapters,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
