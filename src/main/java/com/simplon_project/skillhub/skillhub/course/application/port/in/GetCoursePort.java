package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCourseCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

import java.util.List;

public interface GetCoursePort {
    List<Course> getCourse(GetCourseCommand command);
}
