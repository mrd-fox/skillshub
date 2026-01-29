package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateCourseCommand;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.List;

@Schema(name = "UpdateCourseRequest", description = "PATCH-like payload to update a course. All fields are optional.")
public record UpdateCourseRequest(

        @Schema(description = "New course title. Optional. Cannot be blank if provided.",
                example = "Java Fundamentals - Updated")
        @Nullable
        String title,

        @Schema(description = "New course description. Optional. Cannot be blank if provided.",
                example = "Updated description...")
        @Nullable
        String description,

        @Schema(description = "New price. Optional. 0 means free.",
                example = "0", minimum = "0")
        @Nullable
        @Min(0)
        Long price,

        @ArraySchema(schema = @Schema(implementation = UpdateSectionRequest.class),
                arraySchema = @Schema(description = "Optional sections patch. If omitted => no section changes."))
        @Nullable
        @Valid
        List<UpdateSectionRequest> sections
) {

    public UpdateCourseCommand mapToCommand(String courseId, String externalAuthorId, String rawRoles) {
        return UpdateCourseCommand.builder()
                .courseId(courseId)
                .externalAuthorId(externalAuthorId)
                .rawRoles(rawRoles)
                .title(Helper.normalizeOptional(title))
                .description(Helper.normalizeOptional(description))
                .price(price)
                .sections(UpdateSectionRequest.mapToCommands(sections))
                .build();
    }
}
