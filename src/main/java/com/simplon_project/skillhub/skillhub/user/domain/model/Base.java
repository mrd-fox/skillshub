package com.simplon_project.skillhub.skillhub.user.domain.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public class Base {
    Id id;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
