package com.simplon_project.skillhub.skillhub.course.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public class Section extends Base {
    private Course course;
    private String title;
    @Builder.Default
    private Set<Chapter> chapters = new HashSet<>();

    public void setChapters(List<Chapter> chapters) {
        this.chapters = (chapters == null) ? new HashSet<>() : new HashSet<>(chapters);
    }


    public void addChapter(Chapter chapter) {
        Objects.requireNonNull(chapter, "chapter is required");

        if (chapters == null) {
            chapters = new HashSet<>();
        }

        int maxPos = chapters.isEmpty()
                ? 0
                : chapters.stream().mapToInt(Chapter::getPosition).max().orElse(0);

        var desiredPos = chapter.getPosition();
        int finalPos;

        if (desiredPos == null || desiredPos <= 0 || desiredPos > maxPos + 1) {
            finalPos = maxPos + 1;
        } else {
            finalPos = desiredPos;
            for (Chapter c : chapters) {
                if (c.getPosition() >= finalPos) {
                    c.setPosition(c.getPosition() + 1);
                }
            }
        }

        chapter.setSection(this);
        chapter.setPosition(finalPos);
        chapters.add(chapter);

    }

    public List<Chapter> getChaptersSorted() {
        return chapters.stream()
                .sorted(Comparator.comparingInt(Chapter::getPosition))
                .toList();
    }
}
