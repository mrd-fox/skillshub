package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;


import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.*;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.*;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseRepositoryAdapter implements
        CoursePort,
        LoadCourseByIdPort,
        UpdateCourseStructurePort,
        LoadCourseStructurePort,
        LoadCourseWithVideoPort,
        SoftDeleteCoursePort {

    private final JpaCourseRepository courseJpaRepository;

    // ==================== LoadCourseByIdPort ====================

    @Override
    public Optional<Course> loadCourseById(Id courseId) {
        var idModified = EntityId.fromString(courseId.asString());
        return courseJpaRepository.findById(idModified)
                .map(entity -> CourseEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext()));
    }

    // ==================== UpdateCourseStructurePort ====================

    @Override
    public Course updateCourseStructure(Course courseDomain) {
        var courseId = EntityId.fromString(courseDomain.getId().asString());

        // 1) Load managed aggregate graph
        CourseEntity managedCourseEntity = courseJpaRepository
                .findByIdWithTreeIncludingDeleted(courseId)
                .orElseThrow(() -> new IllegalStateException("Course not found: " + courseId));

        // 2) Apply domain patch to managed entity graph (in-place mutations)
        applyDomainToManagedEntity(courseDomain, managedCourseEntity);

        // 3) Persist + flush to populate timestamps
        var savedCourseEntity = courseJpaRepository.saveAndFlush(managedCourseEntity);

        // 4) Map from persisted entity
        return CourseEntityMapper.mapToDomain(savedCourseEntity, new CycleAvoidingMappingContext());
    }

    // ==================== CoursePort (legacy - compatibility) ====================

    @Override
    public Optional<Course> findById(String id) {
        return loadCourseById(Id.of(id));
    }

    @Override
    public Optional<Course> findByTitle(String title) {
        return courseJpaRepository.findByTitle(title)
                .map(entity -> CourseEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext()));
    }

    @Override
    public Course save(Course courseDomain) {
        return updateCourseStructure(courseDomain);
    }

    private void applyDomainToManagedEntity(Course courseDomain, CourseEntity managedCourseEntity) {

        managedCourseEntity.setTitle(courseDomain.getTitle());
        managedCourseEntity.setDescription(courseDomain.getDescription());
        managedCourseEntity.setStatus(courseDomain.getStatus());

        // Soft delete sync for course itself (CRITICAL for deleteCourse)
        managedCourseEntity.setDeletedAt(courseDomain.getDeletedAt());

        Set<Section> domainSections = (courseDomain.getSections() == null) ? Collections.emptySet() : courseDomain.getSections();

        Map<UUID, Section> sectionDomainsById = domainSections.stream()
                .filter(sectionDomain -> sectionDomain.getId() != null)
                .collect(Collectors.toMap(
                        sectionDomain -> UUID.fromString(sectionDomain.getId().asString()),
                        sectionDomain -> sectionDomain
                ));

        for (Map.Entry<UUID, Section> entry : sectionDomainsById.entrySet()) {
            UUID sectionId = entry.getKey();
            Section sectionDomain = entry.getValue();

            SectionEntity existingSectionEntity = managedCourseEntity.getSections().stream()
                    .filter(sectionEntity -> sectionEntity.getId().value().equals(sectionId))
                    .findFirst()
                    .orElse(null);

            if (existingSectionEntity == null) {

                SectionEntity newSectionEntity = SectionEntity.builder()
                        .sectionId(EntityId.of(sectionId))
                        .title(sectionDomain.getTitle())
                        .course(managedCourseEntity)
                        .position(sectionDomain.getPosition())
                        .deletedAt(sectionDomain.getDeletedAt())
                        .build();

                Set<Chapter> domainChapters = (sectionDomain.getChapters() == null) ? Collections.emptySet() : sectionDomain.getChapters();

                var chapterEntities = domainChapters.stream()
                        .filter(chapterDomain -> chapterDomain.getId() != null)
                        .map(chapterDomain -> {
                            ChapterEntity candidateChapter = ChapterEntity.builder()
                                    .chapterId(EntityId.of(UUID.fromString(chapterDomain.getId().asString())))
                                    .title(chapterDomain.getTitle())
                                    .position(chapterDomain.getPosition())
                                    .deletedAt(chapterDomain.getDeletedAt())
                                    .section(newSectionEntity)
                                    .build();

                            Optional.ofNullable(chapterDomain.getVideo()).ifPresent(videoDomain -> {
                                VideoEntity videoEntity = VideoEntity.builder()
                                        .videoId(EntityId.of(UUID.fromString(videoDomain.id().asString())))
                                        .storageKey(videoDomain.key())
                                        .format(videoDomain.format())
                                        .size(videoDomain.size())
                                        .width(videoDomain.width())
                                        .duration(videoDomain.duration())
                                        .status(videoDomain.status())
                                        .height(videoDomain.height())
                                        .externalDeletionStatus(videoDomain.externalDeletionStatus())
                                        .deletedAt(videoDomain.deletedAt())
                                        .chapter(candidateChapter)
                                        .build();

                                candidateChapter.setVideo(videoEntity);
                            });

                            return candidateChapter;
                        })
                        .toList();

                // Attach chapters once
                newSectionEntity.getChapters().addAll(chapterEntities);

                // Attach section once
                managedCourseEntity.getSections().add(newSectionEntity);

            } else {
                // Update existing section
                if (!Objects.equals(existingSectionEntity.getTitle(), sectionDomain.getTitle())) {
                    existingSectionEntity.setTitle(sectionDomain.getTitle());
                }
                if (!Objects.equals(existingSectionEntity.getPosition(), sectionDomain.getPosition())) {
                    existingSectionEntity.setPosition(sectionDomain.getPosition());
                }

                // Sync soft delete timestamp
                existingSectionEntity.setDeletedAt(sectionDomain.getDeletedAt());

                syncChaptersInPlace(existingSectionEntity, sectionDomain);
            }
        }
    }

    private void syncChaptersInPlace(SectionEntity sectionEntity, Section sectionDomain) {

        Set<Chapter> domainChapters = (sectionDomain.getChapters() == null) ? Collections.emptySet() : sectionDomain.getChapters();

        Map<UUID, Chapter> chaptersById = domainChapters.stream()
                .filter(chapterDomain -> chapterDomain.getId() != null)
                .collect(Collectors.toMap(
                        c -> UUID.fromString(c.getId().asString()),
                        c -> c
                ));

        for (Map.Entry<UUID, Chapter> entry : chaptersById.entrySet()) {
            UUID chapterId = entry.getKey();
            Chapter chapterDomain = entry.getValue();

            ChapterEntity existingChapter = sectionEntity.getChapters().stream()
                    .filter(c -> c.getId().value().equals(chapterId))
                    .findFirst()
                    .orElse(null);

            if (existingChapter == null) {
                ChapterEntity candidateChapter = ChapterEntity.builder()
                        .chapterId(EntityId.of(chapterId))
                        .title(chapterDomain.getTitle())
                        .position(chapterDomain.getPosition())
                        .deletedAt(chapterDomain.getDeletedAt())
                        .section(sectionEntity)
                        .build();

                Optional.ofNullable(chapterDomain.getVideo()).ifPresent(videoDomain -> {
                    VideoEntity videoEntity = VideoEntity.builder()
                            .videoId(EntityId.of(UUID.fromString(videoDomain.id().asString())))
                            .storageKey(videoDomain.key())
                            .format(videoDomain.format())
                            .size(videoDomain.size())
                            .width(videoDomain.width())
                            .duration(videoDomain.duration())
                            .status(videoDomain.status())
                            .height(videoDomain.height())
                            .externalDeletionStatus(videoDomain.externalDeletionStatus())
                            .deletedAt(videoDomain.deletedAt())
                            .chapter(candidateChapter)
                            .build();
                    candidateChapter.setVideo(videoEntity);
                });

                sectionEntity.getChapters().add(candidateChapter);

            } else {
                // Update existing chapter
                if (!Objects.equals(existingChapter.getTitle(), chapterDomain.getTitle())) {
                    existingChapter.setTitle(chapterDomain.getTitle());
                }
                if (!Objects.equals(existingChapter.getPosition(), chapterDomain.getPosition())) {
                    existingChapter.setPosition(chapterDomain.getPosition());
                }

                // Sync soft delete timestamp
                existingChapter.setDeletedAt(chapterDomain.getDeletedAt());

                Optional.ofNullable(chapterDomain.getVideo()).ifPresent(videoDomain -> {
                    VideoEntity existingVideoEntity = existingChapter.getVideo();

                    if (existingVideoEntity == null) {
                        VideoEntity videoEntity = VideoEntity.builder()
                                .videoId(EntityId.of(UUID.fromString(videoDomain.id().asString())))
                                .storageKey(videoDomain.key())
                                .format(videoDomain.format())
                                .size(videoDomain.size())
                                .width(videoDomain.width())
                                .duration(videoDomain.duration())
                                .status(videoDomain.status())
                                .height(videoDomain.height())
                                .externalDeletionStatus(videoDomain.externalDeletionStatus())
                                .deletedAt(videoDomain.deletedAt())
                                .chapter(existingChapter)
                                .build();
                        existingChapter.setVideo(videoEntity);

                    } else {
                        // IMPORTANT:
                        // Do NOT overwrite polling-managed fields (status, duration, thumbnailUrl, dimensions, format, etc.)
                        // Only sync soft-delete/external deletion tracking fields from domain.

                        existingVideoEntity.setExternalDeletionStatus(videoDomain.externalDeletionStatus());
                        existingVideoEntity.setDeletedAt(videoDomain.deletedAt());

                        if (videoDomain.externalDeletionStatus() == com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus.REQUESTED) {
                            if (existingVideoEntity.getDeleteRequestedAt() == null) {
                                existingVideoEntity.setDeleteRequestedAt(Instant.now());
                                existingVideoEntity.setDeleteAttemptCount(0);
                                existingVideoEntity.setDeleteLastError(null);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public Course loadStructure(Id courseId) {
        var entityId = EntityId.fromString(courseId.asString());

        var entity = courseJpaRepository.findByIdWithPublicTree(entityId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return CourseEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext());
    }

    @Override
    public Course loadWithVideo(Id courseId) {
        var entityId = EntityId.fromString(courseId.asString());

        var entity = courseJpaRepository.findByIdWithTree(entityId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return CourseEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext());
    }

    // ==================== SoftDeleteCoursePort ====================

    @Override
    public void softDelete(Course course, Instant now) {
        // Domain already contains the soft-delete cascade timestamps.
        // Reuse the managed-graph patch to persist it.
        updateCourseStructure(course);
    }
}