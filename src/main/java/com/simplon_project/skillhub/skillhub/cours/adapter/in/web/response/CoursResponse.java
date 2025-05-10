package com.simplon_project.skillhub.skillhub.cours.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CoursResponse(
        String id,
        String title,
        String description,
        String status,
        List<SectionResponse> sections
) {
}
