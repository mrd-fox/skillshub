package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.ChapterResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;

import java.util.List;
import java.util.Set;

public final class ChapterResponseMapper {
    public static ChapterResponse mapToChapterResponse(Chapter chapter) {
        return ChapterResponse.builder()
                .id(chapter.getId().asString())
                .title(chapter.getTitle())
                .position(chapter.getPosition())
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .video(chapter.getVideo() != null ? VideoResponseMapper.mapToVideoResponse(chapter.getVideo()) : null)
                .build();
    }

    public static List<ChapterResponse> mapToChapterResponses(Set<Chapter> chapters) {
        return chapters.stream().map(ChapterResponseMapper::mapToChapterResponse).toList();
    }
}
