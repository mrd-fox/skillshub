package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

public interface CreateCoursePort {

    Course createCourse(CreateCourseCommand command);

}
