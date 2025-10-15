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
@Builder
@EqualsAndHashCode(callSuper = true)
public class VideoEntity extends AbstractBaseEntity {
    @EmbeddedId
    private EntityId videoId;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name = "format")
    private String format;

    @Column(name = "size")
    private Long size; // en octets

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "duration")
    private Long duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VideoStatusEnum status;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk__video_chapter__id"))
    private ChapterEntity chapter;

    @Override
    public EntityId getId() {
        return videoId;
    }
}
