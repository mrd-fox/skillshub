package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

import java.util.List;

public record CreateSectionCommand(
        String courseId,
        String title,
        List<CreateChapterCommand> chapters
) {
    public Section mapToDomain() {
        Course course = null;
        if (courseId != null) {
            course = Course.builder().id(Id.of(courseId)).build();
        }
        return Section.builder()
                .id(Id.random())
                .course(course)
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
