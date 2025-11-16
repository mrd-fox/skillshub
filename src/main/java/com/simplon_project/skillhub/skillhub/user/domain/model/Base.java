package com.simplon_project.skillhub.skillhub.user.domain.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public class Base {
    protected Id id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    protected Base(Id id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null in domain entity");
        }
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
