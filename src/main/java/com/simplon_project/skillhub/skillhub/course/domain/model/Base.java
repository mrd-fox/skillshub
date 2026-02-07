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

    /**
     * Mark this entity as deleted by setting the deletedAt timestamp.
     * Used for Option B soft delete strategy.
     */
    public void markAsDeleted() {
        this.deletedAt = Instant.now();
    }

    /**
     * Check if this entity is soft deleted.
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
