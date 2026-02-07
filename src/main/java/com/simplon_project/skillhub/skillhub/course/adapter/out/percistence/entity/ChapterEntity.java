package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "\"chapters\"")
@SQLRestriction("deleted_at is null")
public class ChapterEntity extends AbstractBaseEntity {

    @EmbeddedId
    private EntityId chapterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "position", nullable = false)
    private Integer position;

    // Soft delete support (Option B strategy)
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToOne(mappedBy = "chapter", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private VideoEntity video = null;

    public void setVideo(VideoEntity videoEntity) {
        if (this.video != null) {
            this.video.setChapter(null);
        }
        this.video = videoEntity;
        if (videoEntity != null) {
            videoEntity.setChapter(this);
        }
    }

    @Override
    public EntityId getId() {
        return chapterId;
    }
}