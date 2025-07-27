package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.ChapterResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;

import java.util.List;

public final class ChapterResponseMapper {
    public static ChapterResponse mapToChapterResponse(Chapter chapter) {
        return ChapterResponse.builder()
                .id(chapter.getId().toString())
                .title(chapter.getTitle())
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .videoUrl(chapter.getVideoUrl())
                .build();
    }

    public static List<ChapterResponse> mapToChapterResponses(List<Chapter> chapters) {
        return chapters.stream().map(ChapterResponseMapper::mapToChapterResponse).toList();
    }
}
