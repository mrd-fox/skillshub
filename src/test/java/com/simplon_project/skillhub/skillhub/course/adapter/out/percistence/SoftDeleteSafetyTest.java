package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.*;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SH-166: Tests to guarantee soft-deleted entities are NEVER returned.
 * <p>
 * IMPORTANT:
 * These tests MUST FAIL if soft-delete filtering is accidentally removed.
 * <p>
 * Diagnostic hardening:
 * - We always capture the persisted EmbeddedId before clearing persistence context.
 * - We assert EntityManager.find(...) can retrieve the entity by EmbeddedId.
 * If this fails, the issue is NOT the repository query but the JPA mapping / persistence.
 */
@DataJpaTest(excludeAutoConfiguration = LiquibaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("SH-166: Soft Delete Safety Tests")
public class SoftDeleteSafetyTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaCourseRepository courseRepository;

    /**
     * Hard assertion:
     * If this fails, your mapping of @EmbeddedId / schema is broken.
     */
    private EntityId persistAndAssertFindable(CourseEntity course) {
        CourseEntity persisted = entityManager.persistAndFlush(course);

        EntityId persistedCourseId = persisted.getCourseId();
        assertNotNull(persistedCourseId, "Persisted courseId must not be null");
        assertNotNull(persistedCourseId.value(), "Persisted courseId.value must not be null");

        // EntityManager.find must work BEFORE clear
        CourseEntity emFoundBeforeClear = entityManager.find(CourseEntity.class, persistedCourseId);
        assertNotNull(emFoundBeforeClear, "EntityManager.find must find the course before clear()");

        entityManager.clear();

        // EntityManager.find must work AFTER clear (forces DB round-trip)
        CourseEntity emFoundAfterClear = entityManager.find(CourseEntity.class, persistedCourseId);
        assertNotNull(emFoundAfterClear, "EntityManager.find must find the course after clear()");

        return persistedCourseId;
    }

    @Test
    @DisplayName("Soft-deleted sections must NOT appear in course tree")
    void softDeletedSections_mustNotBeReturned() {
        CourseEntity course = CourseEntity.builder()
                .courseId(EntityId.random())
                .title("Test Course")
                .status(CourseStatusEnum.DRAFT)
                .externalUserId("user-123")
                .sections(new HashSet<>())
                .build();

        SectionEntity activeSection = SectionEntity.builder()
                .sectionId(EntityId.random())
                .title("Active Section")
                .position(1)
                .course(course)
                .deletedAt(null)
                .chapters(new HashSet<>())
                .build();

        SectionEntity deletedSection = SectionEntity.builder()
                .sectionId(EntityId.random())
                .title("Deleted Section")
                .position(2)
                .course(course)
                .deletedAt(Instant.now())
                .chapters(new HashSet<>())
                .build();

        course.getSections().add(activeSection);
        course.getSections().add(deletedSection);

        EntityId courseId = persistAndAssertFindable(course);

        var loadedCourse = courseRepository.findByIdWithTree(courseId);

        assertTrue(loadedCourse.isPresent(), "Course should be found");
        assertThat(loadedCourse.get().getSections()).hasSize(1);
        assertThat(loadedCourse.get().getSections())
                .extracting(SectionEntity::getTitle)
                .containsOnly("Active Section");

        Course domainCourse = CourseEntityMapper.mapToDomain(
                loadedCourse.get(),
                new CycleAvoidingMappingContext()
        );
        assertThat(domainCourse.getSections()).hasSize(1);
    }

    @Test
    @DisplayName("Soft-deleted chapters must NOT appear in section tree")
    void softDeletedChapters_mustNotBeReturned() {
        CourseEntity course = CourseEntity.builder()
                .courseId(EntityId.random())
                .title("Test Course")
                .status(CourseStatusEnum.DRAFT)
                .externalUserId("user-123")
                .sections(new HashSet<>())
                .build();

        SectionEntity section = SectionEntity.builder()
                .sectionId(EntityId.random())
                .title("Test Section")
                .position(1)
                .course(course)
                .deletedAt(null)
                .chapters(new HashSet<>())
                .build();

        ChapterEntity activeChapter = ChapterEntity.builder()
                .chapterId(EntityId.random())
                .title("Active Chapter")
                .position(1)
                .section(section)
                .deletedAt(null)
                .build();

        ChapterEntity deletedChapter = ChapterEntity.builder()
                .chapterId(EntityId.random())
                .title("Deleted Chapter")
                .position(2)
                .section(section)
                .deletedAt(Instant.now())
                .build();

        section.getChapters().add(activeChapter);
        section.getChapters().add(deletedChapter);
        course.getSections().add(section);

        EntityId courseId = persistAndAssertFindable(course);

        var loadedCourse = courseRepository.findByIdWithTree(courseId);

        assertTrue(loadedCourse.isPresent(), "Course should be found");
        var loadedSection = loadedCourse.get().getSections().iterator().next();
        assertThat(loadedSection.getChapters()).hasSize(1);
        assertThat(loadedSection.getChapters())
                .extracting(ChapterEntity::getTitle)
                .containsOnly("Active Chapter");

        Course domainCourse = CourseEntityMapper.mapToDomain(
                loadedCourse.get(),
                new CycleAvoidingMappingContext()
        );
        var domainSection = domainCourse.getSections().iterator().next();
        assertThat(domainSection.getChapters()).hasSize(1);
    }

    @Test
    @DisplayName("Soft-deleted videos must NOT appear in chapter tree")
    void softDeletedVideos_mustNotBeReturned() {
        CourseEntity course = CourseEntity.builder()
                .courseId(EntityId.random())
                .title("Test Course")
                .status(CourseStatusEnum.DRAFT)
                .externalUserId("user-123")
                .sections(new HashSet<>())
                .build();

        SectionEntity section = SectionEntity.builder()
                .sectionId(EntityId.random())
                .title("Test Section")
                .position(1)
                .course(course)
                .chapters(new HashSet<>())
                .build();

        ChapterEntity chapterWithDeletedVideo = ChapterEntity.builder()
                .chapterId(EntityId.random())
                .title("Chapter with Deleted Video")
                .position(1)
                .section(section)
                .deletedAt(null)
                .build();

        VideoEntity deletedVideo = VideoEntity.builder()
                .videoId(EntityId.random())
                .sourceUri("vimeo://123456")
                .status(VideoStatusEnum.READY)
                .externalDeletionStatus(ExternalDeletionStatus.NONE)
                .deletedAt(Instant.now())
                .chapter(chapterWithDeletedVideo)
                .build();

        chapterWithDeletedVideo.setVideo(deletedVideo);
        section.getChapters().add(chapterWithDeletedVideo);
        course.getSections().add(section);

        EntityId courseId = persistAndAssertFindable(course);

        var loadedCourse = courseRepository.findByIdWithTree(courseId);

        assertTrue(loadedCourse.isPresent(), "Course should be found");
        var loadedSection = loadedCourse.get().getSections().iterator().next();
        var loadedChapter = loadedSection.getChapters().iterator().next();
        assertNull(loadedChapter.getVideo(), "Soft-deleted video must NOT be loaded");

        Course domainCourse = CourseEntityMapper.mapToDomain(
                loadedCourse.get(),
                new CycleAvoidingMappingContext()
        );
        var domainSection = domainCourse.getSections().iterator().next();
        var domainChapter = domainSection.getChapters().iterator().next();
        assertNull(domainChapter.getVideo(), "Domain must not have soft-deleted video");
    }

    @Test
    @DisplayName("Soft-deleted course must NOT be returned")
    void softDeletedCourse_mustNotBeReturned() {
        CourseEntity deletedCourse = CourseEntity.builder()
                .courseId(EntityId.random())
                .title("Deleted Course")
                .status(CourseStatusEnum.DRAFT)
                .externalUserId("user-123")
                .deletedAt(Instant.now())
                .sections(new HashSet<>())
                .build();

        // Persistable & findable at EM-level even if soft-deleted (the filtering is at query level, not EM.find)
        CourseEntity persisted = entityManager.persistAndFlush(deletedCourse);
        EntityId courseId = persisted.getCourseId();
        assertNotNull(courseId);
        assertNotNull(courseId.value());

        entityManager.clear();

        // Sanity check: EM.find must still find it by PK
        CourseEntity emFound = entityManager.find(CourseEntity.class, courseId);
        assertNotNull(emFound, "EntityManager.find must find soft-deleted course by PK (no filtering at EM level)");

        var loadedCourse = courseRepository.findByIdWithTree(courseId);

        assertFalse(loadedCourse.isPresent(),
                "Soft-deleted course must NOT be returned by findByIdWithTree");
    }

    @Test
    @DisplayName("Entire subtree of soft-deleted section must be filtered")
    void cascadingSoftDelete_entireSubtreeMustBeFiltered() {
        CourseEntity course = CourseEntity.builder()
                .courseId(EntityId.random())
                .title("Test Course")
                .status(CourseStatusEnum.DRAFT)
                .externalUserId("user-123")
                .sections(new HashSet<>())
                .build();

        SectionEntity deletedSection = SectionEntity.builder()
                .sectionId(EntityId.random())
                .title("Deleted Section")
                .position(1)
                .course(course)
                .deletedAt(Instant.now())
                .chapters(new HashSet<>())
                .build();

        ChapterEntity activeChapter = ChapterEntity.builder()
                .chapterId(EntityId.random())
                .title("Active Chapter")
                .position(1)
                .section(deletedSection)
                .deletedAt(null)
                .build();

        VideoEntity activeVideo = VideoEntity.builder()
                .videoId(EntityId.random())
                .sourceUri("vimeo://999")
                .status(VideoStatusEnum.READY)
                .externalDeletionStatus(ExternalDeletionStatus.NONE)
                .deletedAt(null)
                .chapter(activeChapter)
                .build();

        activeChapter.setVideo(activeVideo);
        deletedSection.getChapters().add(activeChapter);
        course.getSections().add(deletedSection);

        EntityId courseId = persistAndAssertFindable(course);

        var loadedCourse = courseRepository.findByIdWithTree(courseId);

        assertTrue(loadedCourse.isPresent(), "Course should be found");
        assertThat(loadedCourse.get().getSections()).isEmpty();

        Course domainCourse = CourseEntityMapper.mapToDomain(
                loadedCourse.get(),
                new CycleAvoidingMappingContext()
        );
        assertThat(domainCourse.getSections()).isEmpty();
    }
}
