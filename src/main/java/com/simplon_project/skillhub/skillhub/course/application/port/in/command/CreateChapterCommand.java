package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record CreateChapterCommand(
        @NotNull String chapterTitle,
        String courseId,
        String sectionId,
        @NotNull Integer position
) {

    public CreateChapterCommand {

        chapterTitle = Helper.sanitize(chapterTitle);
        if (chapterTitle == null || chapterTitle.isBlank()) {
            throw new IllegalArgumentException("chapter.title is required and cannot be blank");
        }
    }

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

    public static Set<Chapter> mapToDomains(List<CreateChapterCommand> commands) {
        if (commands == null) return new HashSet<>();
        return commands.stream()
                .map(CreateChapterCommand::mapToDomain)
                .collect(Collectors.toSet());
    }
}
