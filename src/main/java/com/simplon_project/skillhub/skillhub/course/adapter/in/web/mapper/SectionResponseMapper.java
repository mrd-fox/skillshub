package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.SectionResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SectionResponseMapper {
    public static SectionResponse mapToSectionResponse(Section section) {
        return SectionResponse.builder()
                .id(section.getId().asString())
                .title(section.getTitle())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .position(section.getPosition())
                .chapters(section.getChapters() != null ? ChapterResponseMapper.mapToChapterResponses(section.getChapters()) : List.of())
                .build();
    }

    public static List<SectionResponse> mapToSectionResponses(Set<Section> sections) {
        return sections.stream().map(SectionResponseMapper::mapToSectionResponse).toList();
    }
}
