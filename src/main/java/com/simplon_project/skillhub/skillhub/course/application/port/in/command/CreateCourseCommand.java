package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.Builder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public record CreateCourseCommand(
        String externalAuthorId,
        String rawRoles,
        String title,
        String description,
        Long price,
        List<CreateSectionCommand> sections

) {


    @Builder
    public CreateCourseCommand {
// --- auth user ---
        if (externalAuthorId == null || externalAuthorId.isBlank()) {
            throw new IllegalArgumentException("X-User-Id is missing or blank");
        }

        // --- roles ---
        if (rawRoles == null || rawRoles.isBlank()) {
            throw new IllegalArgumentException("X-User-Roles is missing or blank");
        }

        Set<UserRole> roles = parseRoles(rawRoles);

        if (!roles.contains(UserRole.TUTOR) && !roles.contains(UserRole.ADMIN)) {
            throw new IllegalArgumentException("Only TUTOR or ADMIN can create a course");
        }

        // --- payload ---
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is missing or blank");
        }

        if (price == null || price < 0) {
            price = 0L;
        }

        if (sections == null) {
            sections = List.of();
        }

    }


    public Course mapToDomain() {
        return Course.builder()
                .id(Id.random())
                .title(title)
                .description(description)
                .price(price)
                .sections(CreateSectionCommand.mapToDomains(sections))
                .externalUserId(externalAuthorId)
                .build();
    }

    public Course mapToDomain(String id) {
        return Course.builder()
                .id(Id.of(id))
                .title(title)
                .description(description)
                .price(price)
                .sections(CreateSectionCommand.mapToDomains(sections))
                .externalUserId(externalAuthorId)
                .build();
    }

    private Set<UserRole> parseRoles(String rawRoles) {
        return Arrays.stream(rawRoles.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(UserRole::valueOf)
                .collect(Collectors.toUnmodifiableSet());
    }
}
