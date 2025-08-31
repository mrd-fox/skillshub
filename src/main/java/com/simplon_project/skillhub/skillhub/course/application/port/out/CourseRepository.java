package com.simplon_project.skillhub.skillhub.course.application.port.out;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

import java.util.Optional;

public interface CourseRepository {
    Optional<Course> findById(String id);

    Optional<Course> findByTitle(String title);

    Course save(Course course);
}
