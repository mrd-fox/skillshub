package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;

import java.util.List;

public record CreateChapterCommand(
        String chapterTitle,
        String courseId,
        String sectionId,
        Integer position
) {
    public Chapter mapToDomain() {
        return Chapter.builder().title(chapterTitle).position(position).build();
    }

    public static List<Chapter> mapToDomains(List<CreateChapterCommand> commands) {
        if (commands == null) return List.of();
        return commands.stream()
                .map(CreateChapterCommand::mapToDomain)
                .toList();
    }
}
