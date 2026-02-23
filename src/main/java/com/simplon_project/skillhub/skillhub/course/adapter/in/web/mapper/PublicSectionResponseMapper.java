package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.PublicSectionResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicSection;

import java.util.List;

public class PublicSectionResponseMapper {

    static PublicSectionResponse mapToResponse(PublicSection domain) {
        return PublicSectionResponse.builder()
                .id(domain.getSectionId().toString())
                .title(domain.getTitle())
                .position(domain.getPosition())
                .chapters(PublicChapterResponseMapper.mapToResponses(domain.getChapters()))
                .build();
    }


    List<PublicSectionResponse> mapToResponses(List<PublicSection> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }
        return domains.stream()
                .map(PublicSectionResponseMapper::mapToResponse)
                .toList();
    }

}
