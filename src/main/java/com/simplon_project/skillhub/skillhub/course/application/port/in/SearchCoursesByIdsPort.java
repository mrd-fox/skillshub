package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.SearchCoursesByIdsCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

import java.util.List;

public interface SearchCoursesByIdsPort {

    List<Course> searchByIds(SearchCoursesByIdsCommand command);

}

