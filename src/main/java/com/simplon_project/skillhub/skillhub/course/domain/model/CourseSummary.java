package com.simplon_project.skillhub.skillhub.course.domain.model;

import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight course representation for list views (student dashboard).
 * Contains only essential metadata without sections/chapters/videos.
 */
@Getter
@RequiredArgsConstructor(staticName = "of")
public final class CourseSummary {

    private final Id courseId;
    private final String title;
    private final String description;
    private final CourseStatusEnum status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}

