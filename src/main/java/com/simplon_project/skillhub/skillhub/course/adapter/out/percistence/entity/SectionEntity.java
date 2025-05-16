package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class SectionEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    private String title;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChapterEntity> chapters = new ArrayList<>();
}
