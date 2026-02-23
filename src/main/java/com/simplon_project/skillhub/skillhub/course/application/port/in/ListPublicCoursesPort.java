package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseSummary;

import java.util.List;

public interface ListPublicCoursesPort {

    List<PublicCourseSummary> listPublicCourses();

}