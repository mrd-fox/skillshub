package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.application.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.*;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.IsUserEnrolledInCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.LoadEnrolledCourseIdsPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.outbox.EnqueueOutboxEventPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.ExistsInFlightVideoForCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.UploadMediaPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.*;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseNotAccessibleException;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseStructureLockedException;
import com.simplon_project.skillhub.skillhub.course.domain.exception.StudentNotEnrolledException;
import com.simplon_project.skillhub.skillhub.course.domain.exception.UnauthorizedCourseAccessException;
import com.simplon_project.skillhub.skillhub.course.domain.model.*;
import com.simplon_project.skillhub.skillhub.course.domain.policy.CourseAccessPolicy;
import com.simplon_project.skillhub.skillhub.course.domain.specification.CoursePublishableSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
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
        GetStudentCoursePort,
        ListPublicCoursesPort,
        GetPublicCourseDetailPort,
        PublishCoursePort,
        DeleteCoursePort,
        SearchCoursesByIdsPort {

    private final CreateNewCoursePort createNewCoursePort;
    private final UpdateCourseStructurePort updateCourseStructurePort;
    private final FindCoursePort findCoursePort;

    private final LoadPublicCoursesPort loadPublicCoursesPort;
    private final LoadPublicCourseDetailPort loadPublicCourseDetailPort;

    private final LoadCourseStructurePort loadCourseStructurePort;
    private final LoadCourseWithVideoPort loadCourseWithVideoPort;

    private final EnqueueOutboxEventPort enqueueOutboxEventPort;
    private final ExistsInFlightVideoForCoursePort existsInFlightVideoForCoursePort;
    private final SoftDeleteCoursePort softDeleteCoursePort;
    private final LoadCoursesByIdsPort loadCoursesByIdsPort;
    private final LoadEnrolledCourseIdsPort loadEnrolledCourseIdsPort;
    private final IsUserEnrolledInCoursePort isUserEnrolledInCoursePort;

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

        Course existing = loadCourseWithVideoPort.loadWithVideo(courseId);

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
            // GUARD: Block structural changes when videos are in-flight
            enforceStructureLock(courseId);

            Instant now = Instant.now();
            applySectionsPatch(existing, command.sections(), now);
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
    @Transactional(value = "courseTxManager", readOnly = true)
    public Course get(GetStudentCourseCommand command) {
        var roles = command.userRoles();

        // Only ADMIN or STUDENT can access
        if (!roles.contains(UserRole.ADMIN) && !roles.contains(UserRole.STUDENT)) {
            throw new UnauthorizedCourseAccessException("Access restricted to ADMIN or STUDENT roles");
        }

        var courseId = UUID.fromString(command.courseId());
        var domainCourseId = Id.of(command.courseId());

        // ADMIN: full access to any course
        if (roles.contains(UserRole.ADMIN)) {
            return loadCourseWithVideoPort.loadWithVideo(domainCourseId);
        }

        // STUDENT: verify enrollment
        var externalUserId = UUID.fromString(command.externalUserId());
        var isEnrolled = isUserEnrolledInCoursePort.isEnrolled(externalUserId, courseId);

        if (!isEnrolled) {
            throw new StudentNotEnrolledException(command.courseId());
        }

        var course = loadCourseWithVideoPort.loadWithVideo(domainCourseId);

        // Students can only access PUBLISHED courses
        if (course.getStatus() != CourseStatusEnum.PUBLISHED) {
            throw new CourseNotAccessibleException(command.courseId());
        }

        return course;
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

    @Transactional("courseTxManager")
    @Override
    public void delete(DeleteCourseCommand command) {
        Id courseId = Id.of(command.courseId());
        Course existing = loadCourseWithVideoPort.loadWithVideo(courseId);

        var roles = Helper.extractUserRoles(command.rawRoles());

        if (roles.contains(UserRole.ADMIN)) {
            // no op
        } else if (roles.contains(UserRole.TUTOR)) {
            if (!Objects.equals(existing.getExternalUserId(), command.externalUserId())) {
                throw new UnauthorizedCourseAccessException("Tutor can only delete their own courses");
            }
        } else {
            throw new UnauthorizedCourseAccessException("Only ADMIN or TUTOR can delete a course");
        }

        Instant now = Instant.now();

        existing.markAsDeleted(now);

        for (Section section : safeSections(existing)) {
            softDeleteSection(section, now);
        }

        softDeleteCoursePort.softDelete(existing, now);
    }

    // ========================================================================
    // STRUCTURE LOCK ENFORCEMENT
    // ========================================================================

    /**
     * Enforce structure lock: block structural updates when videos are in-flight.
     *
     * @param courseId the course ID
     * @throws CourseStructureLockedException if any in-flight video exists
     */
    private void enforceStructureLock(Id courseId) {
        Set<VideoStatusEnum> inFlightStatuses = Set.of(
                VideoStatusEnum.PENDING,
                VideoStatusEnum.PROCESSING
        );

        boolean hasInFlightVideo = existsInFlightVideoForCoursePort.existsInFlightVideoForCourse(
                courseId,
                inFlightStatuses
        );

        if (hasInFlightVideo) {
            throw new CourseStructureLockedException(courseId, inFlightStatuses);
        }
    }

    // ========================================================================
    // PATCH helpers
    // ========================================================================

    private void applySectionsPatch(Course course, List<UpdateSectionCommand> patchSections, Instant now) {

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
                softDeleteSection(existing, now);
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
                    applyChaptersPatch(created, sectionCmd.chapters(), now);
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
                applyChaptersPatch(current, sectionCmd.chapters(), now);
            }
        }
    }

    private void applyChaptersPatch(Section section, List<UpdateChapterCommand> patchChapters, Instant now) {

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
                softDeleteChapter(existing, now);
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

    // ========================================================================
    // Soft delete helpers (domain state + outbox)
    // ========================================================================

    /**
     * Soft delete a section and all its chapters/videos using a consistent timestamp.
     */
    private void softDeleteSection(Section section, Instant now) {
        section.markAsDeleted(now);

        for (Chapter chapter : safeChapters(section)) {
            softDeleteChapter(chapter, now);
        }
    }

    /**
     * Soft delete a chapter and its video (if present) using a consistent timestamp.
     */
    private void softDeleteChapter(Chapter chapter, Instant now) {
        chapter.markAsDeleted(now);

        VideoInfo video = chapter.getVideo();
        if (video != null) {
            VideoInfo updatedVideo = softDeleteVideo(video, now);
            chapter.setVideo(updatedVideo);
        }
    }

    /**
     * Soft delete a video. If external deletion is not yet requested, enqueue outbox once.
     */
    private VideoInfo softDeleteVideo(VideoInfo video, Instant now) {
        ExternalDeletionStatus currentStatus =
                (video.externalDeletionStatus() == null) ? ExternalDeletionStatus.NONE : video.externalDeletionStatus();

        boolean alreadyDeleted = video.deletedAt() != null;

        // Idempotence + consistency:
        // - If already soft-deleted, never enqueue again.
        // - Ensure status is not NONE after soft delete.
        boolean needsRequest = !alreadyDeleted && currentStatus == ExternalDeletionStatus.NONE;

        ExternalDeletionStatus nextStatus;
        if (alreadyDeleted) {
            // Keep existing terminal state if any; otherwise assume REQUESTED (safe default)
            if (currentStatus == ExternalDeletionStatus.NONE) {
                nextStatus = ExternalDeletionStatus.REQUESTED;
            } else {
                nextStatus = currentStatus;
            }
        } else {
            nextStatus = needsRequest ? ExternalDeletionStatus.REQUESTED : currentStatus;
        }

        if (needsRequest) {
            if (video.sourceUri() == null) {
                throw new IllegalStateException(
                        "Video sourceUri is required to enqueue deletion (videoId=" + video.id().asString() + ")"
                );
            }
            enqueueOutboxEventPort.enqueueVideoDeletionRequested(video.id(), video.sourceUri());
        }

        return VideoInfo.builder()
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
                .externalDeletionStatus(nextStatus)
                .deletedAt(now)
                .build();
    }

    // ========================================================================
    // Null-safe accessors
    // ========================================================================

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

    @Override
    @Transactional(readOnly = true, transactionManager = "courseTxManager")
    public List<Course> searchByIds(SearchCoursesByIdsCommand command) {
        // Load enrolled course IDs for the user (fail-closed: exceptions propagate)
        Set<Id> enrolledIds = loadEnrolledCourseIdsPort.loadEnrolledCourseIds(command.externalUserId());

        // Compute intersection: only return courses the user is enrolled in
        Set<Id> requestedIds = new HashSet<>(command.courseIds());
        requestedIds.retainAll(enrolledIds);

        // If no enrolled courses in the requested list, return empty
        if (requestedIds.isEmpty()) {
            return List.of();
        }

        // Load and return only the courses the user is entitled to see
        return loadCoursesByIdsPort.loadCoursesByIds(new ArrayList<>(requestedIds));
    }
}