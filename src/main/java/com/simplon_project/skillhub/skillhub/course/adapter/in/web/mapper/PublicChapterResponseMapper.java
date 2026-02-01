package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.PublicChapterResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicChapter;

import java.util.List;

public class PublicChapterResponseMapper {
    static PublicChapterResponse mapToResponse(PublicChapter domain) {
        return PublicChapterResponse.builder()
                .id(domain.getChapterId().asString())
                .title(domain.getTitle())
                .position(domain.getPosition())
                .build();
    }

    static List<PublicChapterResponse> mapToResponses(List<PublicChapter> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }
        return domains.stream()
                .map(PublicChapterResponseMapper::mapToResponse)
                .toList();
    }
}
