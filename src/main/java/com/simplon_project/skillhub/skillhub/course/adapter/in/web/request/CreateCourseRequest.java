package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record CreateCourseRequest(
        @Schema(example = "Mon title", description = "The title of course", requiredMode = REQUIRED)
        @NotBlank
        String title,
        @Schema(example = "Course description", description = "The description of course")
        String description,
//        List<String> keyWords,
        @Schema(example = "Course price", description = "The price of course")
        Long price,
        @Nullable
        List<CreateSectionRequest> sections
) {
    public CreateCourseRequest {
        if (sections == null) {
            sections = Collections.emptyList();
        }
    }
}
