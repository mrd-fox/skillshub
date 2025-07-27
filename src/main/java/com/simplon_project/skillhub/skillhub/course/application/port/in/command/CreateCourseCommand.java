package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

import java.util.List;

public record CreateCourseCommand(
        String title,
        String description,
        Long price,
        List<CreateSectionCommand> sections

) {
    public Course mapToDomain() {
        return Course.builder()
                .title(title)
                .description(description)
                .price(price)
                .sections(CreateSectionCommand.mapToDomains(sections))
                .build();
    }
}
