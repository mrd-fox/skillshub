package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateCourseCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

public interface UpdateCoursePort {

    Course updateCourse(UpdateCourseCommand command);

}
