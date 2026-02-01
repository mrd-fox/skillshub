package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.*;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.UploadMediaPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.AccessLevelEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.model.*;
import com.simplon_project.skillhub.skillhub.course.domain.policy.CourseAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CourseUseCases implements
        CreateCoursePort,
        UpdateCoursePort,
        UploadMediaPort,
        GetCoursePort,
        ListPublicCoursesPort,
        GetPublicCourseDetailPort {

    private final SaveCoursePort saveCoursePort;
    private final FindCoursePort findCoursePort;

    private final LoadPublicCoursesPort loadPublicCoursesPort;
    private final LoadPublicCourseDetailPort loadPublicCourseDetailPort;

    private final LoadCourseStructurePort loadCourseStructurePort;
    private final LoadCourseWithVideoPort loadCourseWithVideoPort;


    @Transactional("courseTxManager")
    @Override
    public Course createCourse(CreateCourseCommand command) {
        var course = command.mapToDomain();
        saveCoursePort.assertCourseNotExists(course);
        return saveCoursePort.saveCourse(course);
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
                // Uniqueness check is persistence-level, cannot be done in Command
                var candidate = Course.builder().title(command.title()).build();
                saveCoursePort.assertCourseNotExists(candidate);
                existing.setTitle(command.title());
            }

        }

        if (command.description() != null) {
            existing.setDescription(command.description());
        }

        if (command.price() != null) {
            existing.setPrice(command.price());
        }

        if (command.sections() != null) {
            applySectionsPatch(existing, command.sections());
        }

        return saveCoursePort.saveCourse(existing);
    }

    @Override
    public void uploadMedia(String bucket, String key, InputStream stream, long size, String contentType) {

    }

    // This endpoint seems to be "get my courses" for author.
    // Keep as-is if it's already wired with a dedicated query.
    // If you later want "author-only list", enforce roles/scope here too.
    @Override
    public List<Course> getCourses(GetCoursesCommand command) {
        return findCoursePort.findByExternalUserId(command.externalAuthorId());

    }

    @Override
    public Course getCourse(GetCourseCommand command) {
        var courseId = Id.of(command.courseId());
        var externalUserId = command.externalAuthorId();
        var roles = command.userRoles();

        // ADMIN: no ownership, no enrollment, no published checks => direct full load
        if (roles.contains(UserRole.ADMIN)) {
            return loadCourseWithVideoPort.loadWithVideo(courseId);
        }

        // For everyone else we need authorId + status => load structure (no video)
        Course courseStructure = loadCourseStructurePort.loadStructure(courseId);

        // Resolve scope WITHOUT enrollment first (fast)
        var scope = CourseAccessPolicy.resolveAccess(
                roles,
                externalUserId,
                courseStructure.getExternalUserId(),
                false
        );

        // Only compute enrollment if needed
        boolean isEnrolled = false;
        if (scope != AccessLevelEnum.AUTHOR && scope != AccessLevelEnum.ADMIN) {
            if (externalUserId != null) {
                // isEnrolled = checkEnrollmentPort.isEnrolled(courseId, externalUserId);
            }


        }

        // Enforce PUBLISHED for PUBLIC/ENROLLED viewers
        if (scope == AccessLevelEnum.PUBLIC || scope == AccessLevelEnum.ENROLLED) {
            if (courseStructure.getStatus() != CourseStatusEnum.PUBLISHED) {
                throw new CourseNotFoundException(courseId);
            }
        }

        // PUBLIC returns structure only (no video)
        if (scope == AccessLevelEnum.PUBLIC) {
            return courseStructure;
        }

        // ENROLLED/AUTHOR => full tree with video
        return loadCourseWithVideoPort.loadWithVideo(courseId);
    }


    @Override
    public List<PublicCourseSummary> listPublicCourses() {
        return loadPublicCoursesPort.loadPublicCourses();
    }


    @Override
    public PublicCourseDetail getPublicCourseDetail(GetPublicCourseDetailCommand command) {
        return loadPublicCourseDetailPort.loadPublicCourseDetail(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));
    }


// ========================================================================
// PATCH helpers
// ========================================================================

    private void applySectionsPatch(Course course, List<UpdateSectionCommand> patchSections) {

        Map<Id, Section> existingById = safeSections(course).stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(Section::getId, Function.identity(), (a, b) -> a));

        for (UpdateSectionCommand sectionCmd : patchSections) {

            Section patch = sectionCmd.mapToDomain();
            Section current = existingById.get(patch.getId());

            if (current == null) {
                // CREATE
                course.addSection(patch);

                // optional chapters on create
                if (sectionCmd.chapters() != null) {
                    applyChaptersPatch(patch, sectionCmd.chapters());
                }
                continue;
            }

            // UPDATE only provided fields
            if (patch.getTitle() != null) {
                current.setTitle(patch.getTitle());
            }
            if (patch.getPosition() != null) {
                current.setPosition(patch.getPosition());
            }

            // optional chapters patch
            if (sectionCmd.chapters() != null) {
                applyChaptersPatch(current, sectionCmd.chapters());
            }
        }
    }

    private void applyChaptersPatch(Section section, List<UpdateChapterCommand> patchChapters) {

        Map<Id, Chapter> existingById = safeChapters(section).stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Chapter::getId, Function.identity(), (a, b) -> a));

        for (UpdateChapterCommand chapterCmd : patchChapters) {

            Chapter patch = chapterCmd.mapToDomain();
            Chapter current = existingById.get(patch.getId());

            if (current == null) {
                // CREATE
                section.addChapter(patch);
                continue;
            }

            // UPDATE
            if (patch.getTitle() != null) {
                current.setTitle(patch.getTitle());
            }
            if (patch.getPosition() != null) {
                current.setPosition(patch.getPosition());
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
