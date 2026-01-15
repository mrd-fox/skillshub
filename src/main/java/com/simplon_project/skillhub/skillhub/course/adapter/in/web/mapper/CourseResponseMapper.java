package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CourseResponseMapper {

    public static CourseResponse mapToCourseResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId().asString())
                .title(course.getTitle())
                .description(course.getDescription())
                .status(course.getStatus())
                .sections(course.getSections() != null ? SectionResponseMapper.mapToSectionResponses(course.getSections()) : List.of())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    public static List<CourseResponse> mapToCourseResponses(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return List.of();
        }

        return courses.stream()
                .map(CourseResponseMapper::mapToCourseResponse)
                .toList();
    }

}
