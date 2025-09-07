package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

import java.util.List;

public record CreateSectionCommand(
        String courseId,
        String title,
        List<CreateChapterCommand> chapters
) {
    public Section mapToDomain() {
        return Section.builder()
                .id(Id.random())
                .title(title)
                .chapters(CreateChapterCommand.mapToDomains(chapters))
                .build();
    }

    public static List<Section> mapToDomains(List<CreateSectionCommand> commands) {
        if (commands == null) return List.of();
        return commands.stream()
                .map(CreateSectionCommand::mapToDomain)
                .toList();
    }
}
