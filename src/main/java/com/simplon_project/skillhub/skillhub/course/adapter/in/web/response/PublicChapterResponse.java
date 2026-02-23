package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "PublicChapterResponse",
        description = "Public chapter representation inside a public course outline."
)
public record PublicChapterResponse(

        @Schema(
                description = "Unique chapter identifier",
                example = "3a9c2c10-9c1b-4b77-8a9a-2c93f6a1f8b4"
        )
        String id,

        @Schema(
                description = "Chapter title",
                example = "What is Hexagonal Architecture?"
        )
        String title,

        @Schema(
                description = "Chapter position (ordering index)",
                example = "1"
        )
        Integer position

) {
}
