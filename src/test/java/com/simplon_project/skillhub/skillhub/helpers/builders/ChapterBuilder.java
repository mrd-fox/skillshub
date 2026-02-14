package com.simplon_project.skillhub.skillhub.helpers.builders;

import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

import java.time.Instant;
import java.util.UUID;

public class ChapterBuilder {

    private Id id = Id.of(UUID.randomUUID().toString());
    private String title = "Chapter title";
    private Integer position = 1;
    private Instant deletedAt = null;
    private VideoInfo video = null;

    private ChapterBuilder() {
    }

    public static ChapterBuilder aChapter() {
        return new ChapterBuilder();
    }

    public ChapterBuilder withId(String id) {
        this.id = Id.of(id);
        return this;
    }

    public ChapterBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public ChapterBuilder withPosition(Integer position) {
        this.position = position;
        return this;
    }

    public ChapterBuilder deletedNow() {
        this.deletedAt = Instant.now();
        return this;
    }

    public ChapterBuilder deletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public ChapterBuilder withVideo(VideoInfo video) {
        this.video = video;
        return this;
    }

    public ChapterBuilder withoutVideo() {
        this.video = null;
        return this;
    }

    public Chapter build() {
        Chapter chapter = Chapter.builder()
                .id(id)
                .title(title)
                .position(position)
                .deletedAt(deletedAt)
                .build();

        if (video != null) {
            chapter.setVideo(video);
        }

        return chapter;
    }
}