package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "PublicCourseListItemResponse",
        description = "Public catalog course item (metadata only). No authentication required."
)
public record PublicCourseListItemResponse(

        @Schema(
                description = "Unique course identifier",
                example = "c8b1e5f4-3c9a-4f9a-9c3e-8e2c5b8a1a23"
        )
        String id,

        @Schema(
                description = "Course title",
                example = "Spring Boot & Hexagonal Architecture"
        )
        String title,

        @Schema(
                description = "Short course description",
                example = "Learn how to build clean and maintainable Spring Boot applications using hexagonal architecture."
        )
        String description,

        @Schema(
                description = "Course price in cents",
                example = "4999"
        )
        Long price,

        @Schema(
                description = "Course creation date",
                example = "2025-01-10T14:32:00"
        )
        LocalDateTime createdAt

) {
}
