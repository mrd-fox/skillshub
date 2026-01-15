package com.simplon_project.skillhub.skillhub.course.application.port.out;


import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.List;

public interface FindCoursePort {

    Course find(Id id);

    Course findByTitle(Course course);

    List<Course> findByExternalUserId(String externalUserId);
}
