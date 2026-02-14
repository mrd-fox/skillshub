package com.simplon_project.skillhub.skillhub.helpers.builders;

import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SectionBuilder {

    private Id id = Id.of(UUID.randomUUID().toString());
    private String title = "Section title";
    private Integer position = 1;
    private Instant deletedAt = null;

    private final List<Chapter> chapters = new ArrayList<>();

    private SectionBuilder() {
    }

    public static SectionBuilder aSection() {
        return new SectionBuilder();
    }

    public SectionBuilder withId(String id) {
        this.id = Id.of(id);
        return this;
    }

    public SectionBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public SectionBuilder withPosition(Integer position) {
        this.position = position;
        return this;
    }

    public SectionBuilder deletedNow() {
        this.deletedAt = Instant.now();
        return this;
    }

    public SectionBuilder deletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public SectionBuilder withChapter(Chapter chapter) {
        if (chapter != null) {
            this.chapters.add(chapter);
        }
        return this;
    }

    public SectionBuilder withChapters(List<Chapter> chapters) {
        if (chapters != null) {
            this.chapters.addAll(chapters);
        }
        return this;
    }

    public Section build() {
        Section section = Section.builder()
                .id(id)
                .title(title)
                .position(position)
                .deletedAt(deletedAt)
                .build();

        if (!chapters.isEmpty()) {
            if (section.getChapters() == null) {
                section.setChapters(new ArrayList<>());
            }
            section.getChapters().addAll(chapters);
        }

        return section;
    }
}