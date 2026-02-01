package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.PublicCourseDetailResponse;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.PublicCourseListItemResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseSummary;

import java.util.List;

public class PublicCourseResponseMapper {

    public static PublicCourseListItemResponse mapToResponse(PublicCourseSummary domain) {
        return PublicCourseListItemResponse.builder()
                .id(domain.getCourseId().asString())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .build();
    }

    public static List<PublicCourseListItemResponse> mapToResponses(List<PublicCourseSummary> domains) {
        return domains.stream()
                .map(PublicCourseResponseMapper::mapToResponse)
                .toList();
    }

    public static PublicCourseDetailResponse mapToResponse(PublicCourseDetail domain) {
        var sections = domain.getSections().stream()
                .map(PublicSectionResponseMapper::mapToResponse)
                .toList();

        return PublicCourseDetailResponse.builder()
                .id(domain.getCourseId().asString())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .sections(sections)
                .build();
    }
}
