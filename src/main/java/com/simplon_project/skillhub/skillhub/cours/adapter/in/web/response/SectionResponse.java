package com.simplon_project.skillhub.skillhub.cours.adapter.in.web.response;

import java.util.List;

public record SectionResponse(
        String id,
        String title,
        List<ChapterResponse> chapters
) {
}
