package com.simplon_project.skillhub.skillhub.course.domain.model;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.SectionNotFoundException;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public class Course extends Base {
    private String title;
    private String description;
    //    List<String> keyWords;
    // User  author;
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

}
