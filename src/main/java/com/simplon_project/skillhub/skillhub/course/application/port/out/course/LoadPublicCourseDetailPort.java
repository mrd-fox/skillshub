package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;

import java.util.Optional;

public interface LoadPublicCourseDetailPort {

    Optional<PublicCourseDetail> loadPublicCourseDetail(Id courseId);

}
