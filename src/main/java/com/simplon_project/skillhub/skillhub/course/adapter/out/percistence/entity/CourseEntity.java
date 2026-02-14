package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Inheritance(strategy = InheritanceType.JOINED)
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NamedEntityGraph(
        name = "Course.withSectionsChaptersVideos",
        attributeNodes = @NamedAttributeNode(value = "sections", subgraph = "sectionsGraph"),
        subgraphs = {
                @NamedSubgraph(
                        name = "sectionsGraph",
                        attributeNodes = @NamedAttributeNode(value = "chapters", subgraph = "chaptersGraph")
                ),
                @NamedSubgraph(
                        name = "chaptersGraph",
                        attributeNodes = @NamedAttributeNode("video")
                )
        }
)
public class CourseEntity extends AbstractBaseEntity {
    @EmbeddedId
    private EntityId courseId;

    @Column(name = "title", nullable = false, unique = true, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price")
    private Long price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CourseStatusEnum status;

    @Column(name = "external_user_id")
    private String externalUserId;

    // Soft delete support (Option B strategy)
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("position ASC")
    @Builder.Default
    private Set<SectionEntity> sections = new HashSet<>();

    public boolean isPublished() {
        return this.status == CourseStatusEnum.PUBLISHED;
    }

    @Override
    public EntityId getId() {
        return courseId;
    }

}
