package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.List;

public interface LoadCoursesByIdsPort {

    List<Course> loadCoursesByIds(List<Id> courseIds);

}

