package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(
        name = "user_course",
        indexes = {
                @Index(name = "idx_user_course_user_id", columnList = "user_id"),
                @Index(name = "idx_user_course_course_id", columnList = "course_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCourseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private UserCourseId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Pre-persist callback to ensure createdAt is always set.
     * This avoids relying on provider-specific annotations.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
