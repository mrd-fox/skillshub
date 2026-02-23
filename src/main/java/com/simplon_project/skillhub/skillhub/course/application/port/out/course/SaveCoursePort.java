package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

public interface SaveCoursePort {
    void assertCourseNotExists(Course course);

    Course saveCourse(Course course);
}
