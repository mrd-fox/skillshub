package com.simplon_project.skillhub.skillhub.course.application.port.out.course;


import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

import java.util.List;

public interface FindCoursePort {


    Course findByTitle(Course course);

    List<Course> findByExternalUserId(String externalUserId);

}
