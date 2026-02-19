package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Lightweight course summary for student dashboard grid")
public record CourseSummaryResponse(
        @Schema(description = "Course unique identifier", example = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2")
        String id,

        @Schema(description = "Course title", example = "Introduction to Spring Boot")
        String title,

        @Schema(description = "Course description", example = "Learn Spring Boot fundamentals")
        String description,

        @Schema(description = "Course publication status", example = "PUBLISHED")
        CourseStatusEnum status,

        @Schema(description = "Course creation timestamp", example = "2026-02-18T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Course last update timestamp", example = "2026-02-18T14:45:00")
        LocalDateTime updatedAt
) {
}

