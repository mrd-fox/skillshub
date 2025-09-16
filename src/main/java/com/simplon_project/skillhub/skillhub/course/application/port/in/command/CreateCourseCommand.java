package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateCourseCommand(
        String title,
        String description,
        Long price,
        List<CreateSectionCommand> sections

) {
    public Course mapToDomain() {
        return Course.builder()
                .id(Id.random())
                .title(title)
                .description(description)
                .price(price)
                .sections(CreateSectionCommand.mapToDomains(sections))
                .build();
    }

    public Course mapToDomain(String id) {
        return Course.builder()
                .id(Id.of(id))
                .title(title)
                .description(description)
                .price(price)
                .sections(CreateSectionCommand.mapToDomains(sections))
                .build();
    }
}
