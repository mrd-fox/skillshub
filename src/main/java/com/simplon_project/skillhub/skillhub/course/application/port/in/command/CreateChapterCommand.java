package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

import java.util.List;

public record CreateChapterCommand(
        String chapterTitle,
        String courseId,
        String sectionId,
        Integer position
) {
    public Chapter mapToDomain() {
        Section section = null;
        if (courseId != null && sectionId != null) {
            var course = Course.builder().id(Id.of(courseId)).build();
            section = Section.builder().id(Id.of(sectionId)).course(course).build();
        }

        return Chapter.builder().title(chapterTitle).position(position).section(section).build();
    }

    public static List<Chapter> mapToDomains(List<CreateChapterCommand> commands) {
        if (commands == null) return List.of();
        return commands.stream()
                .map(CreateChapterCommand::mapToDomain)
                .toList();
    }
}
