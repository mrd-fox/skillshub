package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.PublishCourseCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

public interface PublishCoursePort {

    Course publishCourse(PublishCourseCommand command);
}
