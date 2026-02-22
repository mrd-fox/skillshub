package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.config.helper.DateTimeHelper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.CourseSummary;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.List;

/**
 * Lightweight mapper for converting CourseEntity to CourseSummary.
 * Does NOT load sections/chapters/videos - optimized for list views.
 */
public class CourseSummaryEntityMapper {

    /**
     * Map a single CourseEntity to CourseSummary (lightweight, no relations).
     */
    public static CourseSummary mapToDomain(CourseEntity entity) {
        return CourseSummary.of(
                Id.of(entity.getId().value().toString()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStatus(),
                DateTimeHelper.toLocalDateTime(entity.getCreatedAt()),
                DateTimeHelper.toLocalDateTime(entity.getUpdatedAt())
        );
    }

    /**
     * Map a list of CourseEntity to CourseSummary list.
     */
    public static List<CourseSummary> mapToDomains(List<CourseEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(CourseSummaryEntityMapper::mapToDomain)
                .toList();
    }
}

