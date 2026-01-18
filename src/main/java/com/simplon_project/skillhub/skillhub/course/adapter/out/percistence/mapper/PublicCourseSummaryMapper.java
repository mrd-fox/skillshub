package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseSummary;

/**
 * Mapper local au module persistence: Entity -> Domain(publiccatalog).
 * Pas de DTO web ici, pas de usecase ici.
 */
public class PublicCourseSummaryMapper {

    private PublicCourseSummaryMapper() {
    }

    public static PublicCourseSummary mapToDomain(CourseEntity entity) {
        return PublicCourseSummary.of(
                Id.of(entity.getCourseId().toString()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice()
        );
    }


}