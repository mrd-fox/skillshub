package com.simplon_project.skillhub.skillhub.cours.application.usecase;

import com.simplon_project.skillhub.skillhub.cours.application.port.in.CreateCoursePort;
import com.simplon_project.skillhub.skillhub.cours.application.port.out.FindCoursePort;
import com.simplon_project.skillhub.skillhub.cours.domain.model.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CourseUseCases implements CreateCoursePort {

    private final FindCoursePort findCoursePort;

    @Override
    public Course createCourse(Course course) {
        assertCourseNotExists(course.getTitle());
        return saveCoursePort.save(course);
    }

    private void assertCourseNotExists(String title) {
        var exists = findCoursePort.existsByTitleNot(title);
        if (exists) {
            throw new CoursAlreadyExistsException(title);
        }

    }
}
