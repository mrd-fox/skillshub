package com.simplon_project.skillhub.skillhub.user.application.port.in;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.EnrollInCourseCommand;

/**
 * Input port for enrolling a user in a course.
 */
public interface EnrollInCoursePort {

    /**
     * Enroll a user in a course.
     *
     * @param command the enrollment command containing user and course information
     */
    void enroll(EnrollInCourseCommand command);
}

