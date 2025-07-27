package com.simplon_project.skillhub.skillhub.course.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class Base {
    Id id;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
