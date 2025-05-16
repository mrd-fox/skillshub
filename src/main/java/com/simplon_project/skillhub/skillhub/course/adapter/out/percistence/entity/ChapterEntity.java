package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "\"chapters\"")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class ChapterEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String videoUrl;

}
