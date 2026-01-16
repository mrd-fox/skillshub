package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "\"videos\"")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class VideoEntity extends AbstractBaseEntity {

    @EmbeddedId
    private EntityId videoId;

    // Legacy MinIO/S3 storage key (optional). Keep nullable to support Vimeo-only flows.
    @Column(name = "storage_key")
    private String storageKey;

    // Canonical provider-agnostic URI (required, unique).
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

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * A chapter can have 0..1 video. Video is associated after init/upload flow.
     * Keep nullable=true to allow PENDING rows before linkage.
     */
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