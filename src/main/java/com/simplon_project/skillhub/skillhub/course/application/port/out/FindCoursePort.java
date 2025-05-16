package com.simplon_project.skillhub.skillhub.course.application.port.out;


import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

public interface FindCoursePort {


    Course findCourse(String title);
}
