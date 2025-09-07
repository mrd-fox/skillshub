package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "\"chapters\"")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class ChapterEntity extends BaseEntity implements Persistable<UUID> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "position", nullable = false)
    private Integer position;

    @OneToOne(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private VideoEntity video;

    public void setVideo(VideoEntity videoEntity) {
        if (this.video != null) this.video.setChapter(null);
        this.video = videoEntity;
        if (videoEntity != null) videoEntity.setChapter(this);
    }

    @Transient
    private boolean isNew = true;

    @Override
    @Transient
    public boolean isNew() {
        return isNew;
    }


    public void markNew() {
        this.isNew = true;
    }

    public void markNotNew() {
        this.isNew = false;
    }


    @PostLoad
    @PostPersist
    void afterLoadOrPersist() {
        this.isNew = false;
    }


}
