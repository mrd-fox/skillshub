package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseSummary;

import java.util.List;

public interface LoadPublicCoursesPort {

    List<PublicCourseSummary> loadPublicCourses();

}