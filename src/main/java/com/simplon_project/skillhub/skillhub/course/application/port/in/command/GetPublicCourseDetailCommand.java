package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.Objects;


public record GetPublicCourseDetailCommand(
        Id courseId
) {

    public GetPublicCourseDetailCommand {
        Objects.requireNonNull(courseId, "courseId is required");
    }

    public static GetPublicCourseDetailCommand of(String courseId) {
        Objects.requireNonNull(courseId, "courseId is required");
        return new GetPublicCourseDetailCommand(Id.of(courseId));
    }
}