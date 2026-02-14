package com.simplon_project.skillhub.skillhub.helpers.builders;

import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CourseBuilder {

    private Id id = Id.of(UUID.randomUUID().toString());
    private String externalUserId = "external-user-id";
    private String title = "Course title";
    private String description = "Course description";
    private Long price = 0L;
    private CourseStatusEnum status = CourseStatusEnum.DRAFT;
    private Instant deletedAt = null;

    private final List<Section> sections = new ArrayList<>();

    private CourseBuilder() {
    }

    public static CourseBuilder aCourse() {
        return new CourseBuilder();
    }

    public CourseBuilder withId(String id) {
        this.id = Id.of(id);
        return this;
    }

    public CourseBuilder withExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
        return this;
    }

    public CourseBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public CourseBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public CourseBuilder withPrice(Long price) {
        this.price = price;
        return this;
    }

    public CourseBuilder withStatus(CourseStatusEnum status) {
        this.status = status;
        return this;
    }

    public CourseBuilder deletedNow() {
        this.deletedAt = Instant.now();
        return this;
    }

    public CourseBuilder deletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public CourseBuilder withSection(Section section) {
        if (section != null) {
            this.sections.add(section);
        }
        return this;
    }

    public CourseBuilder withSections(List<Section> sections) {
        if (sections != null) {
            this.sections.addAll(sections);
        }
        return this;
    }

    public Course build() {
        Course course = Course.builder()
                .id(id)
                .externalUserId(externalUserId)
                .title(title)
                .description(description)
                .price(price)
                .status(status)
                .deletedAt(deletedAt)
                .build();

        if (!sections.isEmpty()) {
            if (course.getSections() == null) {
                course.setSections(new java.util.LinkedHashSet<>());
            }
            course.getSections().addAll(sections);
        }

        return course;
    }
}
