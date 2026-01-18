package com.simplon_project.skillhub.skillhub.course.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(staticName = "of")
public final class PublicSection {

    private final Id sectionId;
    private final String title;
    private final Integer position;
    private final List<PublicChapter> chapters;
}
