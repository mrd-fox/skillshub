package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateChapterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record CreateChapterRequest(
        @Schema(example = "Title of chapter", description = "The title of chapter", requiredMode = REQUIRED)
        @NotBlank
        String title
) {

    public CreateChapterCommand toChapterCommand(String courseId, String sectionId) {

        return new CreateChapterCommand(title, courseId, sectionId);
    }

    public static List<CreateChapterCommand> toChapterCommands(List<CreateChapterRequest> requests, String courseId, String sectionId) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(request -> request.toChapterCommand(courseId, sectionId))
                .toList();
    }

}
