package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.application.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.*;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateChapterCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateSectionCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.outbox.EnqueueOutboxEventPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.UploadMediaPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.AccessLevelEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.exception.UnauthorizedCourseAccessException;
import com.simplon_project.skillhub.skillhub.course.domain.model.*;
import com.simplon_project.skillhub.skillhub.course.domain.policy.CourseAccessPolicy;
import com.simplon_project.skillhub.skillhub.course.domain.specification.CoursePublishableSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Course application use cases (orchestration layer).
 * <p>
 * PATCH semantics (CRITICAL):
 * - sections == null  => NO change to course sections
 * - sections != null  => apply patch; missing existing sections are SOFT-DELETED
 * <p>
 * For each section patch:
 * - sectionCmd.chapters == null => NO change to chapters of that section
 * - sectionCmd.chapters != null => apply patch; missing existing chapters are SOFT-DELETED
 * <p>
 * IMPORTANT:
 * Do NOT use mapToDomain() to compute "which ids are present in the patch".
 * mapToDomain() generates random ids for creations, which breaks "missing = deleted" detection.
 * Presence detection must rely on raw command ids (String id fields) only.
 */
@RequiredArgsConstructor
@Service
public class CourseUseCases implements
        CreateCoursePort,
        UpdateCoursePort,
        UploadMediaPort,
        GetCoursePort,
        ListPublicCoursesPort,
        GetPublicCourseDetailPort,
        PublishCoursePort {

    private final CreateNewCoursePort createNewCoursePort;
    private final UpdateCourseStructurePort updateCourseStructurePort;
    private final FindCoursePort findCoursePort;

    private final LoadPublicCoursesPort loadPublicCoursesPort;
    private final LoadPublicCourseDetailPort loadPublicCourseDetailPort;

    private final LoadCourseStructurePort loadCourseStructurePort;
    private final LoadCourseWithVideoPort loadCourseWithVideoPort;

    private final EnqueueOutboxEventPort enqueueOutboxEventPort;

    @Transactional("courseTxManager")
    @Override
    public Course createCourse(com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand command) {
        var course = command.mapToDomain();
        createNewCoursePort.assertCourseNotExists(course);
        return createNewCoursePort.createNewCourse(course);
    }

    @Transactional("courseTxManager")
    @Override
    public Course updateCourse(UpdateCourseCommand command) {

        var courseId = Id.of(command.courseId());

        var existing = loadCourseWithVideoPort.loadWithVideo(courseId);
        if (existing == null) {
            throw new CourseNotFoundException(courseId);
        }

        var roles = Helper.extractUserRoles(command.rawRoles());

        // Authorization: ADMIN can update any course, TUTOR only own course
        if (!roles.contains(UserRole.ADMIN)) {
            if (!Objects.equals(existing.getExternalUserId(), command.externalAuthorId())) {
                throw new CourseNotFoundException(courseId);
            }
        }

        if (command.title() != null) {
            if (!Objects.equals(command.title(), existing.getTitle())) {
                // Uniqueness check is persistence-level; cannot be done in Command
                var candidate = Course.builder().title(command.title()).build();
                createNewCoursePort.assertCourseNotExists(candidate);
                existing.setTitle(command.title());
            }
        }

        if (command.description() != null) {
            existing.setDescription(command.description());
        }

        if (command.price() != null) {
            existing.setPrice(command.price());
        }

        // Patch sections ONLY when client provided them (null = no-op)
        if (command.sections() != null) {
            applySectionsPatch(existing, command.sections());
        }

        return updateCourseStructurePort.updateCourseStructure(existing);
    }

    @Override
    public void uploadMedia(String bucket, String key, InputStream stream, long size, String contentType) {
        // not implemented in this slice
    }

    @Override
    public List<Course> getCourses(com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCoursesCommand command) {
        return findCoursePort.findByExternalUserId(command.externalAuthorId());
    }

    @Override
    public Course getCourse(com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCourseCommand command) {
        var courseId = Id.of(command.courseId());
        var externalUserId = command.externalAuthorId();
        var roles = command.userRoles();

        // ADMIN: direct full load
        if (roles.contains(UserRole.ADMIN)) {
            return loadCourseWithVideoPort.loadWithVideo(courseId);
        }

        // Everyone else: load structure first (no video)
        Course courseStructure = loadCourseStructurePort.loadStructure(courseId);

        var scope = CourseAccessPolicy.resolveAccess(
                roles,
                externalUserId,
                courseStructure.getExternalUserId(),
                false
        );

        // Enforce PUBLISHED for PUBLIC/ENROLLED viewers
        if (scope == AccessLevelEnum.PUBLIC || scope == AccessLevelEnum.ENROLLED) {
            if (courseStructure.getStatus() != CourseStatusEnum.PUBLISHED) {
                throw new CourseNotFoundException(courseId);
            }
        }

        if (scope == AccessLevelEnum.PUBLIC) {
            return courseStructure;
        }

        return loadCourseWithVideoPort.loadWithVideo(courseId);
    }

    @Override
    public List<PublicCourseSummary> listPublicCourses() {
        return loadPublicCoursesPort.loadPublicCourses();
    }

    @Override
    public PublicCourseDetail getPublicCourseDetail(com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetPublicCourseDetailCommand command) {
        return loadPublicCourseDetailPort.loadPublicCourseDetail(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));
    }

    @Transactional("courseTxManager")
    @Override
    public Course publishCourse(com.simplon_project.skillhub.skillhub.course.application.port.in.command.PublishCourseCommand command) {
        var courseId = Id.of(command.courseId());
        var externalUserId = command.externalUserId();
        var roles = command.userRoles();

        Course course = loadCourseWithVideoPort.loadWithVideo(courseId);
        if (course == null) {
            throw new CourseNotFoundException(courseId);
        }

        boolean isAdmin = roles.contains(UserRole.ADMIN);
        boolean isTutorOwner = roles.contains(UserRole.TUTOR)
                && Objects.equals(course.getExternalUserId(), externalUserId);

        if (!isAdmin && !isTutorOwner) {
            if (roles.contains(UserRole.TUTOR)) {
                throw new UnauthorizedCourseAccessException("Tutor can only publish their own courses");
            } else {
                throw new UnauthorizedCourseAccessException("Only ADMIN or TUTOR can publish courses");
            }
        }

        CoursePublishableSpecification.check(course);
        course.markAsWaitingValidation();

        return updateCourseStructurePort.updateCourseStructure(course);
    }

    // ========================================================================
    // PATCH helpers
    // ========================================================================

    private void applySectionsPatch(Course course, List<UpdateSectionCommand> patchSections) {

        Map<Id, Section> existingById = safeSections(course).stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(Section::getId, Function.identity(), (a, b) -> a));

        /*
         * PATCH PRESENCE DETECTION (DO NOT MAP TO DOMAIN HERE):
         * We must use raw ids coming from the payload. Mapping generates random ids for creations.
         */
        Set<Id> patchSectionIds = patchSections.stream()
                .map(UpdateSectionCommand::id)
                .filter(Objects::nonNull)
                .map(Id::of)
                .collect(Collectors.toSet());

        // Soft delete removed sections (and their chapters/videos)
        for (Section existing : existingById.values()) {
            if (!patchSectionIds.contains(existing.getId())) {
                softDeleteSection(existing);
            }
        }

        for (UpdateSectionCommand sectionCmd : patchSections) {

            // Existing section => id is present in command
            Section current = (sectionCmd.id() == null) ? null : existingById.get(Id.of(sectionCmd.id()));

            if (current == null) {
                // CREATE
                Section created = sectionCmd.mapToDomain();
                course.addSection(created);

                /*
                 * For a created section:
                 * - chapters == null => no chapters created (no-op)
                 * - chapters != null => create chapters based on patch list
                 */
                if (sectionCmd.chapters() != null) {
                    applyChaptersPatch(created, sectionCmd.chapters());
                }
                continue;
            }

            // UPDATE only provided fields
            if (sectionCmd.title() != null) {
                current.setTitle(sectionCmd.title());
            }
            if (sectionCmd.position() != null) {
                current.setPosition(sectionCmd.position());
            }

            /*
             * IMPORTANT PATCH RULE:
             * - chapters == null => client did NOT send chapters => no change
             * - chapters != null => apply patch (missing => soft-delete)
             */
            if (sectionCmd.chapters() != null) {
                applyChaptersPatch(current, sectionCmd.chapters());
            }
        }
    }

    /**
     * Soft delete a section and all its chapters/videos.
     */
    private void softDeleteSection(Section section) {
        section.markAsDeleted();

        for (Chapter chapter : safeChapters(section)) {
            softDeleteChapter(chapter);
        }
    }

    /**
     * Soft delete a chapter and its video (if present).
     */
    private void softDeleteChapter(Chapter chapter) {
        chapter.markAsDeleted();

        VideoInfo video = chapter.getVideo();
        if (video != null) {
            VideoInfo updatedVideo = softDeleteVideo(video);
            chapter.setVideo(updatedVideo);
        }
    }

    /**
     * Soft delete a video and enqueue external deletion request (Outbox).
     * <p>
     * Notes:
     * - This method only updates domain state (VideoInfo) and enqueues outbox.
     * - Persistence occurs later when updateCourseStructurePort.updateCourseStructure(existing) saves the whole aggregate.
     */
    private VideoInfo softDeleteVideo(VideoInfo video) {

        VideoInfo updatedVideo = VideoInfo.builder()
                .id(video.id())
                .sourceUri(video.sourceUri())
                .key(video.key())
                .duration(video.duration())
                .format(video.format())
                .size(video.size())
                .width(video.width())
                .height(video.height())
                .thumbnailUrl(video.thumbnailUrl())
                .embedHash(video.embedHash())
                .errorMessage(video.errorMessage())
                .status(video.status())
                .externalDeletionStatus(ExternalDeletionStatus.REQUESTED)
                .deletedAt(Instant.now())
                .build();

        enqueueOutboxEventPort.enqueueVideoDeletionRequested(video.id(), video.sourceUri());

        return updatedVideo;
    }

    private void applyChaptersPatch(Section section, List<UpdateChapterCommand> patchChapters) {

        Map<Id, Chapter> existingById = safeChapters(section).stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Chapter::getId, Function.identity(), (a, b) -> a));

        /*
         * PATCH PRESENCE DETECTION (DO NOT MAP TO DOMAIN HERE):
         * Use raw command ids to detect removals.
         */
        Set<Id> patchChapterIds = patchChapters.stream()
                .map(UpdateChapterCommand::id)
                .filter(Objects::nonNull)
                .map(Id::of)
                .collect(Collectors.toSet());

        // Soft delete removed chapters (and their videos)
        for (Chapter existing : existingById.values()) {
            if (!patchChapterIds.contains(existing.getId())) {
                softDeleteChapter(existing);
            }
        }

        for (UpdateChapterCommand chapterCmd : patchChapters) {

            Chapter current = (chapterCmd.id() == null) ? null : existingById.get(Id.of(chapterCmd.id()));

            if (current == null) {
                // CREATE
                Chapter created = chapterCmd.mapToDomain();
                section.addChapter(created);
                continue;
            }

            // UPDATE only provided fields
            if (chapterCmd.title() != null) {
                current.setTitle(chapterCmd.title());
            }
            if (chapterCmd.position() != null) {
                current.setPosition(chapterCmd.position());
            }
        }
    }

    private Set<Section> safeSections(Course course) {
        if (course.getSections() == null) {
            return Set.of();
        }
        return course.getSections();
    }

    private Set<Chapter> safeChapters(Section section) {
        if (section.getChapters() == null) {
            return Set.of();
        }
        return section.getChapters();
    }
}