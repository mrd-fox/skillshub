package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetPublicCourseDetailPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.ListPublicCoursesPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCoursesCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetPublicCourseDetailCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.UploadMediaPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.AccessLevelEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseSummary;
import com.simplon_project.skillhub.skillhub.course.domain.policy.CourseAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CourseUseCases implements
        CreateCoursePort,
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
}
