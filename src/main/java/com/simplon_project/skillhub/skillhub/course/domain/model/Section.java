package com.simplon_project.skillhub.skillhub.course.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public class Section extends Base {
    private Course course;
    private String title;
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();

    public void setChapters(List<Chapter> chapters) {
        this.chapters = (chapters == null) ? new ArrayList<>() : new ArrayList<>(chapters);
    }


    public void addChapter(Chapter chapter) {
        Objects.requireNonNull(chapter, "chapter is required");

        if (chapters == null) {
            chapters = new ArrayList<>();
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
        chapters.sort(Comparator.comparingInt(Chapter::getPosition));
    }
}
