package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetPublicCourseDetailCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;

public interface GetPublicCourseDetailPort {

    PublicCourseDetail getPublicCourseDetail(GetPublicCourseDetailCommand command);

}