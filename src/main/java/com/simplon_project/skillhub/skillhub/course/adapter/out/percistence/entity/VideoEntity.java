package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@Entity
@Table(name = "\"videos\"")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@Builder
public class VideoEntity extends AbstractBaseEntity {
    @EmbeddedId
    private EntityId videoId;

    private String storageKey;

    private String format;

    private Long size; // en octets

    private Integer width;

    private Integer height;

    private Duration duration;

    @Enumerated(EnumType.STRING)
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
