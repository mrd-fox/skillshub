package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity;

import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class CourseEntity extends BaseEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

//    @Column(name = "key_words")
//    private List<String> keyWords = new ArrayList<>();

    @Column(name = "price")
    private Long price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CourseStatusEnum status;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SectionEntity> sections = new ArrayList<>();

}
