package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;

public final class PublicCourseEntityDetailMapper {

    private PublicCourseEntityDetailMapper() {
    }

    public static PublicCourseDetail mapToDomain(CourseEntity entity) {
        var sections = PublicSectionEntityDetailMapper
                .mapToDomains(entity.getSections().stream().toList());

        return PublicCourseDetail.of(
                Id.of(entity.getCourseId().toString()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                sections
        );
    }
}
