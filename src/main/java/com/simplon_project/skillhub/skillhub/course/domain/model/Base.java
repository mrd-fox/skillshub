package com.simplon_project.skillhub.skillhub.course.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
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
    Instant deletedAt;  // Soft delete support

    public void markAsDeleted() {
        this.deletedAt = Instant.now();
    }

    public void markAsDeleted(Instant now) {
        this.deletedAt = now;
    }

    /**
     * Check if this entity is soft deleted.
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
