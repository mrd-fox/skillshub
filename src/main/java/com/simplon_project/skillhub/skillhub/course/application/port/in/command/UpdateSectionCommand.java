package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;
import lombok.Builder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record UpdateSectionCommand(
        String id,
        String title,
        Integer position,
        List<UpdateChapterCommand> chapters
) {

    @Builder
    public UpdateSectionCommand {

        id = Helper.normalizeOptional(id);
        title = Helper.normalizeOptional(title);

        if (position != null && position <= 0) {
            throw new IllegalArgumentException("section.position must be > 0");
        }

        // CREATE
        if (id == null) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("section.title is required when creating a section");
            }
        } else {
            // UPDATE
            if (title != null && title.isBlank()) {
                throw new IllegalArgumentException("section.title cannot be blank");
            }
        }

        if (chapters != null) {
            chapters = chapters.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    public Section mapToDomain() {
        return Section.builder()
                .id(id == null ? Id.random() : Id.of(id))
                .title(title)
                .position(position)
                .chapters(UpdateChapterCommand.mapToDomains(chapters))
                .build();
    }

    public static Set<Section> mapToDomains(List<UpdateSectionCommand> commands) {
        if (commands == null) {
            return null;
        }

        Set<String> updateIds = new HashSet<>();
        for (UpdateSectionCommand s : commands) {
            if (s.id != null && !updateIds.add(s.id)) {
                throw new IllegalArgumentException("Duplicate section.id: " + s.id);
            }
        }

        return commands.stream()
                .map(UpdateSectionCommand::mapToDomain)
                .collect(Collectors.toSet());
    }
}

