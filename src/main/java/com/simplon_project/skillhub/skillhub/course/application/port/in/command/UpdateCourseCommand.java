package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.Builder;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public record UpdateCourseCommand(

        // --- auth ---
        String externalAuthorId,
        String rawRoles,

        // --- path ---
        String courseId,

        // --- payload (PATCH-like) ---
        String title,
        String description,
        Long price,
        List<UpdateSectionCommand> sections
) {

    @Builder
    public UpdateCourseCommand {

        // ---- auth ----
        if (externalAuthorId == null || externalAuthorId.isBlank()) {
            throw new IllegalArgumentException("X-User-Id is missing or blank");
        }

        Set<UserRole> roles = Helper.extractUserRoles(rawRoles);
        if (!roles.contains(UserRole.TUTOR) && !roles.contains(UserRole.ADMIN)) {
            throw new IllegalArgumentException("Only TUTOR or ADMIN can update a course");
        }

        // ---- path ----
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId is missing or blank");
        }

        // ---- normalization + sanitization ----
        String rawTitle = title;  // Keep original to check if user tried to send blank
        title = Helper.normalizeOptional(title);
        description = Helper.normalizeOptional(description);

        // title cannot be removed - if provided but blank, it's an error
        if (rawTitle != null && title == null) {
            throw new IllegalArgumentException("title cannot be blank");
        }

        // price: null = no update, 0 = free
        if (price != null && price < 0) {
            throw new IllegalArgumentException("price must be >= 0");
        }

        // sections optional
        if (sections != null) {
            sections = sections.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        validateNotEmpty(title, description, price, sections);
    }

    public Course mapToDomain() {
        return Course.builder()
                .id(Id.of(courseId))
                .title(title)
                .description(description)
                .price(price)
                .sections(UpdateSectionCommand.mapToDomains(sections))
                .externalUserId(externalAuthorId)
                .build();
    }

    private static void validateNotEmpty(
            String title,
            String description,
            Long price,
            List<UpdateSectionCommand> sections
    ) {
        if (title == null && description == null && price == null && sections == null) {
            throw new IllegalArgumentException("UpdateCourseCommand is empty");
        }
    }
}

