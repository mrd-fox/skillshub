package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "\"videos\"")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SQLRestriction("deleted_at is null")
public class VideoEntity extends AbstractBaseEntity {

    @EmbeddedId
    private EntityId videoId;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "source_uri", nullable = false, unique = true, length = 512)
    private String sourceUri;

    @Column(name = "format")
    private String format;

    @Column(name = "size")
    private Long size;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "duration")
    private Long duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VideoStatusEnum status;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "embed_hash", length = 255)
    private String embedHash;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "external_deletion_status", nullable = false, length = 50)
    @Builder.Default
    private ExternalDeletionStatus externalDeletionStatus = ExternalDeletionStatus.NONE;

    @Column(name = "delete_requested_at")
    private Instant deleteRequestedAt;

    @Column(name = "delete_attempt_count", nullable = false)
    @Builder.Default
    private Integer deleteAttemptCount = 0;

    @Column(name = "delete_last_error", columnDefinition = "TEXT")
    private String deleteLastError;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "chapter_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk__video_chapter__id")
    )
    private ChapterEntity chapter;

    @Override
    public EntityId getId() {
        return videoId;
    }
}