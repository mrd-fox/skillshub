package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.SaveCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.UploadMediaPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@RequiredArgsConstructor
@Service
public class CourseUseCases implements CreateCoursePort, UploadMediaPort {

    private final SaveCoursePort saveCoursePort;

    @Transactional
    @Override
    public Course createCourse(CreateCourseCommand command) {
        var course = command.mapToDomain();
        saveCoursePort.assertCourseNotExists(course);
        return saveCoursePort.saveCourse(course);
    }

    @Override
    public void uploadMedia(String bucket, String key, InputStream stream, long size, String contentType) {

    }
}
