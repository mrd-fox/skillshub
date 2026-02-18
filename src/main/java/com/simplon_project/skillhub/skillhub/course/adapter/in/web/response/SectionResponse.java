package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Course section containing ordered chapters")
public record SectionResponse(
        @Schema(description = "Section unique identifier", example = "8b7f2c3a-1e6d-4e9f-9b2e-1d9c4b2a7f11")
        String id,

        @Schema(description = "Section title", example = "Getting Started")
        String title,

        @Schema(description = "Section position in course", example = "1")
        Integer position,

        @Schema(description = "List of chapters in this section")
        List<ChapterResponse> chapters,

        @Schema(description = "Section creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Section last update timestamp")
        LocalDateTime updatedAt

) {
}
