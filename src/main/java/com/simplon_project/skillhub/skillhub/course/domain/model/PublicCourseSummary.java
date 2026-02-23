package com.simplon_project.skillhub.skillhub.course.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public final class PublicCourseSummary {

    private final Id courseId;

    private final String title;

    private final String description;

    private final Long price;
}
