package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record CreateSectionCommand(
        String courseId,
        @NotBlank String title,
        @NotNull @Positive Integer position,
        List<CreateChapterCommand> chapters
) {

    public CreateSectionCommand {
        title = Helper.sanitize(title);
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("section.title is required and cannot be blank");
        }
    }

    public Section mapToDomain() {
        return Section.builder()
                .id(Id.random())
                .title(title)
                .position(position)
                .chapters(CreateChapterCommand.mapToDomains(chapters))
                .build();
    }

    public static Set<Section> mapToDomains(List<CreateSectionCommand> commands) {
        if (commands == null) return new HashSet<>();
        return commands.stream()
                .map(CreateSectionCommand::mapToDomain)
                .collect(Collectors.toSet());
    }
}
