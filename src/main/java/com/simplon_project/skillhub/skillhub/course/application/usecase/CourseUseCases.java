package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetPublicCourseDetailPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.ListPublicCoursesPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetPublicCourseDetailCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.FindCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.LoadPublicCourseDetailPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.LoadPublicCoursesPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.SaveCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.UploadMediaPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseSummary;
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

    @Override
    public List<Course> getCourse(GetCourseCommand command) {
        return findCoursePort.findByExternalUserId(command.externalAuthorId());

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
