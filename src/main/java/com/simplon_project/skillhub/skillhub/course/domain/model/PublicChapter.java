package com.simplon_project.skillhub.skillhub.course.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public final class PublicChapter {

    private final Id chapterId;
    private final String title;
    private final Integer position;
}
