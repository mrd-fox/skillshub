package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.Builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record UpdateChapterCommand(
        String id,
        String title,
        Integer position
) {

    @Builder
    public UpdateChapterCommand {

        id = Helper.normalizeOptional(id);

        // Keep raw title to detect if user sent blank/HTML-only title
        String rawTitle = title;
        title = Helper.normalizeOptional(title);

        if (position != null && position <= 0) {
            throw new IllegalArgumentException("chapter.position must be > 0");
        }

        if (id == null) {
            // CREATE - title is required
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("chapter.title is required when creating a chapter");
            }
        } else {
            // UPDATE - if title provided but becomes blank after sanitization, reject it
            if (rawTitle != null && title == null) {
                throw new IllegalArgumentException("chapter.title cannot be blank");
            }
        }
    }

    public Chapter mapToDomain() {
        return Chapter.builder()
                .id(id == null ? Id.random() : Id.of(id))
                .title(title)
                .position(position)
                .build();
    }

    public static Set<Chapter> mapToDomains(List<UpdateChapterCommand> commands) {
        if (commands == null) {
            return null;
        }

        Set<String> updateIds = new HashSet<>();
        for (UpdateChapterCommand c : commands) {
            if (c.id != null && !updateIds.add(c.id)) {
                throw new IllegalArgumentException("Duplicate chapter.id: " + c.id);
            }
        }

        return commands.stream()
                .map(UpdateChapterCommand::mapToDomain)
                .collect(Collectors.toSet());
    }
}