package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CourseSummaryEntityMapper Unit Tests")
class CourseSummaryEntityMapperTest {

    @Test
    @DisplayName("Should map CourseEntity to CourseSummary with correct fields")
    void shouldMapEntityToDomain() {
        // GIVEN
        var entityId = EntityId.of(UUID.randomUUID());
        var createdAt = Instant.now().minusSeconds(3600);
        var updatedAt = Instant.now();

        var entity = CourseEntity.builder()
                .courseId(entityId)
                .title("Spring Boot Course")
                .description("Learn Spring Boot fundamentals")
                .status(CourseStatusEnum.PUBLISHED)
                .externalUserId("tutor-123")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // WHEN
        var summary = CourseSummaryEntityMapper.mapToDomain(entity);

        // THEN
        assertThat(summary).isNotNull();
        assertThat(summary.getCourseId().asString()).isEqualTo(entityId.value().toString());
        assertThat(summary.getTitle()).isEqualTo("Spring Boot Course");
        assertThat(summary.getDescription()).isEqualTo("Learn Spring Boot fundamentals");
        assertThat(summary.getStatus()).isEqualTo(CourseStatusEnum.PUBLISHED);
        assertThat(summary.getCreatedAt()).isNotNull();
        assertThat(summary.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should map list of CourseEntity to list of CourseSummary")
    void shouldMapEntityListToDomainList() {
        // GIVEN
        var entity1 = CourseEntity.builder()
                .courseId(EntityId.of(UUID.randomUUID()))
                .title("Course 1")
                .description("Description 1")
                .status(CourseStatusEnum.PUBLISHED)
                .externalUserId("tutor-1")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        var entity2 = CourseEntity.builder()
                .courseId(EntityId.of(UUID.randomUUID()))
                .title("Course 2")
                .description("Description 2")
                .status(CourseStatusEnum.DRAFT)
                .externalUserId("tutor-2")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        var entities = List.of(entity1, entity2);

        // WHEN
        var summaries = CourseSummaryEntityMapper.mapToDomains(entities);

        // THEN
        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).getTitle()).isEqualTo("Course 1");
        assertThat(summaries.get(0).getStatus()).isEqualTo(CourseStatusEnum.PUBLISHED);
        assertThat(summaries.get(1).getTitle()).isEqualTo("Course 2");
        assertThat(summaries.get(1).getStatus()).isEqualTo(CourseStatusEnum.DRAFT);
    }

    @Test
    @DisplayName("Should return empty list when entity list is null")
    void shouldReturnEmptyListWhenNull() {
        // WHEN
        var summaries = CourseSummaryEntityMapper.mapToDomains(null);

        // THEN
        assertThat(summaries).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when entity list is empty")
    void shouldReturnEmptyListWhenEmpty() {
        // WHEN
        var summaries = CourseSummaryEntityMapper.mapToDomains(List.of());

        // THEN
        assertThat(summaries).isEmpty();
    }

    @Test
    @DisplayName("Should not include sections in CourseSummary mapping")
    void shouldNotIncludeSections() {
        // GIVEN
        var entity = CourseEntity.builder()
                .courseId(EntityId.of(UUID.randomUUID()))
                .title("Course with sections")
                .description("Description")
                .status(CourseStatusEnum.PUBLISHED)
                .externalUserId("tutor-123")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        // Note: Even if entity had sections, CourseSummary has no sections field

        // WHEN
        var summary = CourseSummaryEntityMapper.mapToDomain(entity);

        // THEN
        assertThat(summary).isNotNull();
        assertThat(summary.getTitle()).isEqualTo("Course with sections");
        // CourseSummary has no getSections() method - this is the lightweight nature
    }
}

