package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;


import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record CreateCourseRequest(
        @Schema(example = "Mon title", description = "The title of course", requiredMode = REQUIRED)
        @NotBlank
        String title,
        @Schema(example = "Course description", description = "The description of course")
        String description,
        @Schema(example = "Course price", description = "The price of course")
        Long price,
        @Schema(example = "Sections of cours", description = "The course's sections")
        List<CreateSectionRequest> sections
) {

    public CreateCourseCommand toCourseCommand() {
        return new CreateCourseCommand(
                title,
                description,
                price,
                CreateSectionRequest.toSectionCommands(sections, null, null)
        );
    }

}
