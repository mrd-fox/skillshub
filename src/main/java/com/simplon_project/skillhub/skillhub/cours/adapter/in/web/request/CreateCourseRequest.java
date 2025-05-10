package com.simplon_project.skillhub.skillhub.cours.adapter.in.web.request;


import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder
public record CreateCourseRequest(
        String title,
        String description,
        List<String> keyWords,
        Long price,
        List<CreateSectionRequest> sections
) {
    public CreateCourseRequest {
        if (sections == null) {
            sections = Collections.emptyList();
        }
    }
}
