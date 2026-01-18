package com.simplon_project.skillhub.skillhub.course.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(staticName = "of")
public final class PublicCourseDetail {

    private final Id courseId;
    private final String title;
    private final String description;
    private final Long price;
    private final List<PublicSection> sections;
}