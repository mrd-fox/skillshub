package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "PublicSectionResponse",
        description = "Public section representation inside a public course outline."
)
public record PublicSectionResponse(

        @Schema(
                description = "Unique section identifier",
                example = "8b7f2c3a-1e6d-4e9f-9b2e-1d9c4b2a7f11"
        )
        String id,

        @Schema(
                description = "Section title",
                example = "Introduction"
        )
        String title,

        @Schema(
                description = "Section position (ordering index)",
                example = "1"
        )
        Integer position,

        @Schema(
                description = "Ordered list of chapters in this section (public outline)",
                implementation = PublicChapterResponse.class
        )
        List<PublicChapterResponse> chapters

) {
}