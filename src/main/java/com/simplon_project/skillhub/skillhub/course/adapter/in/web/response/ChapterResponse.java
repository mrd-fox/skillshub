package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Chapter with associated video content")
public record ChapterResponse(
        @Schema(description = "Chapter unique identifier", example = "7c5d9a2b-3f8e-4d1c-9b6a-2e7f4c8d1a5b")
        String id,

        @Schema(description = "Chapter title", example = "Introduction to the Course")
        String title,

        @Schema(description = "Chapter position in section", example = "1")
        Integer position,

        @Schema(description = "Chapter creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Chapter last update timestamp")
        LocalDateTime updatedAt,

        @Schema(description = "Associated video for playback")
        VideoResponse video
) {
}
