package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import javax.annotation.Nullable;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record CreateSectionRequest(
        @Schema(example = "Title of section", description = "The title of section", requiredMode = REQUIRED)
        @NotBlank
        String title,
        @Nullable
        List<CreateChapterRequest> chapters
) {
}
