package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record CreateSectionCommand(
        String courseId,
        @NotNull String title,
        @NotNull Integer position,
        List<CreateChapterCommand> chapters
) {
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
