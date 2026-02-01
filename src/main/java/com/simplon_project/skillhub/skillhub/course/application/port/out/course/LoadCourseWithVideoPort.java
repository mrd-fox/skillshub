package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

public interface LoadCourseWithVideoPort {

    Course loadWithVideo(Id courseId);
}
