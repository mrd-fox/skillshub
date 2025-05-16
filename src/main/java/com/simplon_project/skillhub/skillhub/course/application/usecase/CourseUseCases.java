package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.SaveCoursePort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CourseUseCases implements CreateCoursePort {

    private final SaveCoursePort saveCoursePort;

    @Override
    public Course createCourse(Course course) {
        saveCoursePort.assertCourseNotExists(course);
        return saveCoursePort.saveCourse(course);
    }

}
