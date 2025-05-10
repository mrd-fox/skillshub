package com.simplon_project.skillhub.skillhub.cours.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class Chapter extends Base {
    String title;
    String videoUrl; //encoded?
}
