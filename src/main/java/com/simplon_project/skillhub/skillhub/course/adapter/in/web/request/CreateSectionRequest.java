package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateSectionCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record CreateSectionRequest(
        @Schema(example = "Title of section", description = "The title of section", requiredMode = REQUIRED)
        @NotBlank
        String title,
        List<CreateChapterRequest> chapters
) {

    public CreateSectionCommand toSectionCommand(String courseId, String sectionId) {
        return new CreateSectionCommand(
                courseId,
                title,
                CreateChapterRequest.toChapterCommands(chapters, courseId, sectionId));
    }

    public static List<CreateSectionCommand> toSectionCommands(List<CreateSectionRequest> requests, String courseId, String sectionId) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(request -> request.toSectionCommand(courseId, sectionId))
                .toList();
    }
}
