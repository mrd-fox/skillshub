package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.FindCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.SaveCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.UploadMediaPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CourseUseCases implements CreateCoursePort, UploadMediaPort, GetCoursePort {

    private final SaveCoursePort saveCoursePort;
    private final FindCoursePort findCoursePort;

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
}
