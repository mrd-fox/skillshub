package com.simplon_project.skillhub.skillhub.cours.application.port.in;

import com.simplon_project.skillhub.skillhub.cours.domain.model.Course;

public interface CreateCoursePort {

    Course createCourse(Course course);
}
