package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@NamedEntityGraph(
        name = "Section.withChaptersVideo",
        attributeNodes = @NamedAttributeNode(value = "chapters", subgraph = "chaptersGraph"),
        subgraphs = {
                @NamedSubgraph(
                        name = "chaptersGraph",
                        attributeNodes = @NamedAttributeNode("video")
                )
        }
)
@Entity
@Table(name = "sections")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class SectionEntity extends AbstractBaseEntity {

    @EmbeddedId
    private EntityId sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(name = "title", nullable = false)
    private String title;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<ChapterEntity> chapters = new ArrayList<>();


    @Override
    public EntityId getId() {
        return sectionId;
    }
}
