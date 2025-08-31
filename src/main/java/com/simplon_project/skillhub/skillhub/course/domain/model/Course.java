package com.simplon_project.skillhub.skillhub.course.domain.model;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.SectionNotFoundException;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class Course extends Base {
    String title;
    String description;
    //    List<String> keyWords;
    // User  author;
    Long price;
    CourseStatusEnum status;
    List<Section> sections;

    public Section getSectionById(Id sectionId) {
        return getSections().stream()
                .filter(section -> section.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(() -> new SectionNotFoundException(sectionId));
    }


}
