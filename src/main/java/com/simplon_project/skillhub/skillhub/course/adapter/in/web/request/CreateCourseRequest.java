package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;
import java.util.Set;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateCourseRequest(
        @Schema(example = "Mon title", description = "The title of course", requiredMode = REQUIRED)
        @NotBlank
        String title,
        @Schema(example = "Course description", description = "The description of course")
        String description,
        @Schema(example = "Course price", description = "The price of course")
        Long price,
        List<CreateSectionRequest> sections
) {

    public CreateCourseCommand toCourseCommand(String externalAuthorId, String roles) {
        return new CreateCourseCommand(
                externalAuthorId,
                roles,
                title,
                description,
                price,
                CreateSectionRequest.toSectionCommands(sections, null, null)
        );
    }

}
