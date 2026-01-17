package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;


import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.*;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseRepositoryAdapter implements CourseRepository {

    private final JpaCourseRepository courseJpaRepository;

    @Override
    public Optional<Course> findById(String id) {
        var idModified = EntityId.fromString(id);
        return courseJpaRepository.findById(idModified)
                .map(entity -> CourseEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext()));
    }

    @Override
    public Optional<Course> findByTitle(String title) {
        return courseJpaRepository.findByTitle(title)
                .map(entity -> CourseEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext()));
    }

    @Override
    public Course save(Course courseDomain) {
        var courseId = EntityId.fromString(courseDomain.getId().asString());

        // 1) Charger le graphe MANAGÉ
        CourseEntity managedCourseEntity = courseJpaRepository
                .findByIdWithTree(courseId)
                .orElseThrow(() -> new IllegalStateException("Course not found: " + courseId));

        // 2) Appliquer un patch ciblé (modifie les collections en place)
        applyDomainToManagedEntity(courseDomain, managedCourseEntity);

        // 3) Persister et flusher pour peupler les timestamps
        var savedCourseEntity = courseJpaRepository.saveAndFlush(managedCourseEntity);

        // 4) Mapper depuis l’entity persistée
        return CourseEntityMapper.mapToDomain(savedCourseEntity, new CycleAvoidingMappingContext());
    }

    private void applyDomainToManagedEntity(Course courseDomain, CourseEntity managedCourseEntity) {

        managedCourseEntity.setTitle(courseDomain.getTitle());
        managedCourseEntity.setDescription(courseDomain.getDescription());
        managedCourseEntity.setStatus(courseDomain.getStatus());


        Map<UUID, Section> sectionDomainsById = courseDomain.getSections().stream()
                .collect(Collectors.toMap(
                        sectionDomain -> UUID.fromString(sectionDomain.getId().asString()),
                        sectionDomain -> sectionDomain
                ));

        for (Map.Entry<UUID, Section> entry : sectionDomainsById.entrySet()) {
            var sectionId = entry.getKey();
            var sectionDomain = entry.getValue();

            var existingSectionEntity = managedCourseEntity.getSections().stream()
                    .filter(sectionEntity -> sectionEntity.getId().value().equals(sectionId))
                    .findFirst()
                    .orElse(null);

            if (existingSectionEntity == null) {

                var newSectionEntity = SectionEntity.builder()
                        .sectionId(EntityId.of(sectionId))
                        .title(sectionDomain.getTitle())
                        .course(managedCourseEntity)
                        .build();

                var chapterEntities = sectionDomain.getChapters().stream()
                        .map(chapterDomain -> {
                            var candidateChapter = ChapterEntity.builder()
                                    .chapterId(EntityId.of(UUID.fromString(chapterDomain.getId().asString())))
                                    .title(chapterDomain.getTitle())
                                    .position(chapterDomain.getPosition())
                                    .build();

                            var candidateVideo = Optional.ofNullable(chapterDomain.getVideo());

                            candidateVideo.ifPresent(videoDomain -> {
                                var videoEntity = VideoEntity.builder()
                                        .videoId(EntityId.of(UUID.fromString(videoDomain.id().asString())))
                                        .storageKey(videoDomain.key())
                                        .format(videoDomain.format())
                                        .size(videoDomain.size())
                                        .width(videoDomain.width())
                                        .duration(videoDomain.duration())
                                        .status(videoDomain.status())
                                        .height(videoDomain.height())
                                        .chapter(candidateChapter)
                                        .build();

                                candidateChapter.setVideo(videoEntity);
                            });

                            candidateChapter.setSection(newSectionEntity);
                            return candidateChapter;
                        }).toList();

                newSectionEntity.getChapters().addAll(chapterEntities);
                managedCourseEntity.getSections().add(newSectionEntity);


                // Rattacher la section pour le FK et remplir la collection modifiable
                for (ChapterEntity chapterEntity : chapterEntities) {
                    chapterEntity.setSection(newSectionEntity);
                }
                newSectionEntity.getChapters().addAll(chapterEntities);
                managedCourseEntity.getSections().add(newSectionEntity);
            } else {
                if (!Objects.equals(existingSectionEntity.getTitle(), sectionDomain.getTitle())) {
                    existingSectionEntity.setTitle(sectionDomain.getTitle());
                }
                syncChaptersInPlace(existingSectionEntity, sectionDomain);
            }
        }
    }

    private void syncChaptersInPlace(SectionEntity sectionEntity, Section sectionDomain) {
        Map<UUID, Chapter> chaptersById = sectionDomain.getChapters().stream()
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
                var candidateChapter = ChapterEntity.builder()
                        .chapterId(EntityId.of(chapterId))
                        .title(chapterDomain.getTitle())
                        .position(chapterDomain.getPosition())
                        .section(sectionEntity)
                        .build();

                var videoCandidate = Optional.ofNullable(chapterDomain.getVideo());
                videoCandidate.ifPresent(videoDomain -> {
                    var videoEntity = VideoEntity.builder()
                            .videoId(EntityId.of(UUID.fromString(videoDomain.id().asString())))
                            .storageKey(videoDomain.key())
                            .format(videoDomain.format())
                            .size(videoDomain.size())
                            .width(videoDomain.width())
                            .duration(videoDomain.duration())
                            .status(videoDomain.status())
                            .height(videoDomain.height())
                            .chapter(candidateChapter)
                            .build();
                    candidateChapter.setVideo(videoEntity);
                });

                sectionEntity.getChapters().add(candidateChapter);
            } else {
                if (!Objects.equals(existingChapter.getTitle(), chapterDomain.getTitle())) {
                    existingChapter.setTitle(chapterDomain.getTitle());
                }
                if (!Objects.equals(existingChapter.getPosition(), chapterDomain.getPosition())) {
                    existingChapter.setPosition(chapterDomain.getPosition());
                }

                var videoCandidate = Optional.ofNullable(chapterDomain.getVideo());
                videoCandidate.ifPresent(videoDomain -> {
                    var existingVideoEntity = existingChapter.getVideo();
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
                                .chapter(existingChapter)
                                .build();
                        existingChapter.setVideo(videoEntity);
                    } else {
                        existingVideoEntity.setStorageKey(videoDomain.key());
                        existingVideoEntity.setFormat(videoDomain.format());
                        existingVideoEntity.setSize(videoDomain.size());
                        existingVideoEntity.setWidth(videoDomain.width());
                        existingVideoEntity.setHeight(videoDomain.height());
                        existingVideoEntity.setDuration(videoDomain.duration());
                        existingVideoEntity.setStatus(videoDomain.status());
                    }
                });
            }
        }
    }
}
