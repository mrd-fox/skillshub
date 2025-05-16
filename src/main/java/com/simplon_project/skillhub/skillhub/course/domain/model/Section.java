package com.simplon_project.skillhub.skillhub.course.domain.model;

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
public class Section extends Base {
    Course course;
    String title;
    List<Chapter> chapters;
}
