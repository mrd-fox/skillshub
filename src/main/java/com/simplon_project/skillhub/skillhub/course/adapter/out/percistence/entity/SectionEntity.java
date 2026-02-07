package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "position", nullable = false)
    private Integer position;

    // Soft delete support (Option B strategy)
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "section", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("position ASC")
    @Builder.Default
    private Set<ChapterEntity> chapters = new HashSet<>();


    @Override
    public EntityId getId() {
        return sectionId;
    }
}
