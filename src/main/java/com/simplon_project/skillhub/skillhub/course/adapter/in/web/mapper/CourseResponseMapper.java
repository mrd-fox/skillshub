package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseSummaryResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.CourseSummary;
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
                .price(course.getPrice())
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

    public static CourseSummaryResponse mapToCourseSummaryResponse(CourseSummary summary) {
        return CourseSummaryResponse.builder()
                .id(summary.getCourseId().asString())
                .title(summary.getTitle())
                .description(summary.getDescription())
                .price(summary.getPrice())
                .status(summary.getStatus())
                .createdAt(summary.getCreatedAt())
                .updatedAt(summary.getUpdatedAt())
                .build();
    }

    public static List<CourseSummaryResponse> mapToCourseSummaryResponses(List<CourseSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return List.of();
        }

        return summaries.stream()
                .map(CourseResponseMapper::mapToCourseSummaryResponse)
                .toList();
    }
}
