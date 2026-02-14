package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

import java.time.Instant;

public interface SoftDeleteCoursePort {
    void softDelete(Course course, Instant now);
}
