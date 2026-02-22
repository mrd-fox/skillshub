package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetStudentCourseCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

/**
 * Input port for retrieving a course for an authenticated student.
 * Enforces enrollment verification and PUBLISHED status.
 */
public interface GetStudentCoursePort {

    /**
     * Get a course for a student (enrolled user only).
     *
     * @param command the validated command with courseId, externalUserId, and roles
     * @return the full course tree including video details
     * @throws "MissingUserContextException"       if user context is missing
     * @throws "UnauthorizedCourseAccessException" if user lacks STUDENT role
     * @throws "StudentNotEnrolledException"       if user is not enrolled in the course
     * @throws "CourseNotFoundException"           if course not found or soft-deleted
     * @throws "CourseNotAccessibleException"      if course is not PUBLISHED
     */
    Course get(GetStudentCourseCommand command);
}

