package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.DeleteCourseCommand;


public interface DeleteCoursePort {

    void delete(DeleteCourseCommand command);
}

