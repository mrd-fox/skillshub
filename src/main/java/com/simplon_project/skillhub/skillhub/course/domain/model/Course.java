package com.simplon_project.skillhub.skillhub.course.domain.model;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.SectionNotFoundException;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public class Course extends Base {
    private String title;
    private String description;
    String externalUserId;
    private Long price;
    @Builder.Default
    private CourseStatusEnum status = CourseStatusEnum.DRAFT;
    @Builder.Default
    private Set<Section> sections = new HashSet<>();


    public Section getSectionById(Id sectionId) {
        return getSections().stream()
                .filter(section -> section.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(() -> new SectionNotFoundException(sectionId));
    }

    public void addSection(Section section) {
        Objects.requireNonNull(section, "section is required");

        if (sections == null) {
            sections = new HashSet<>();
        }

        int maxPos = sections.isEmpty()
                ? 0
                : sections.stream()
                .map(Section::getPosition)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        Integer desiredPos = section.getPosition();
        int finalPos;

        if (desiredPos == null || desiredPos <= 0 || desiredPos > maxPos + 1) {
            finalPos = maxPos + 1;
        } else {
            finalPos = desiredPos;
            for (Section s : sections) {
                Integer p = s.getPosition();
                if (p != null && p >= finalPos) {
                    s.setPosition(p + 1);
                }
            }
        }

        section.setCourse(this);
        section.setPosition(finalPos);
        sections.add(section);
    }

    public List<Section> getSectionsSorted() {
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }

        return sections.stream()
                .sorted(Comparator.comparingInt(s -> s.getPosition() == null ? Integer.MAX_VALUE : s.getPosition()))
                .toList();
    }

}
