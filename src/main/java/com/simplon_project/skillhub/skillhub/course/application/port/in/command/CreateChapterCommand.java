package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.List;

public record CreateChapterCommand(
        String chapterTitle,
        String courseId,
        String sectionId,
        Integer position
) {
    public Chapter mapToDomain() {
        return Chapter.builder()
                .id(Id.random())
                .title(chapterTitle)
                .position(position)
                .build();
    }

    public Chapter mapToDomain(Id id) {
        return Chapter.builder()
                .id(id)
                .title(chapterTitle)
                .position(position)
                .build();
    }

    public static List<Chapter> mapToDomains(List<CreateChapterCommand> commands) {
        if (commands == null) return List.of();
        return commands.stream()
                .map(CreateChapterCommand::mapToDomain)
                .toList();
    }
}
