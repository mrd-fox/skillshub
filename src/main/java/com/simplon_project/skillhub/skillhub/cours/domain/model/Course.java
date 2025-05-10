package com.simplon_project.skillhub.skillhub.cours.domain.model;

import com.simplon_project.skillhub.skillhub.cours.domain.enums.CoursStatusEnum;
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
    List<String> keyWords;
    // User  author;
    Long price;
    CoursStatusEnum statusCours;
    List<Section> sections;
}
