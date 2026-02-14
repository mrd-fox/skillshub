package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.config.helper.DateTimeHelper;
import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.SectionEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.List;
import java.util.UUID;


public class CourseEntityMapper {

    public static Course mapToDomain(CourseEntity entity, CycleAvoidingMappingContext context) {
        var existing = context.getMappedInstance(entity, Course.class);
        if (existing != null) return existing;

        var domain = Course.builder()
                .id(Id.of(entity.getId().value().toString()))
                .status(entity.getStatus())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .createdAt(DateTimeHelper.toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(DateTimeHelper.toLocalDateTime(entity.getUpdatedAt()))
                .externalUserId(entity.getExternalUserId())
                .build();
        context.storeMappedInstance(entity, domain);

        // SAFETY (Secondary Defense): Filter out soft-deleted sections
        // Primary defense is repository queries, but this ensures no deleted entities reach domain
        var nonDeletedSections = entity.getSections().stream()
                .filter(s -> s.getDeletedAt() == null)
                .collect(java.util.stream.Collectors.toSet());

        domain.setSections(SectionEntityMapper.mapToDomains(nonDeletedSections, context));
        return domain;
    }


    public static CourseEntity mapToEntity(Course domain, CycleAvoidingMappingContext context) {
        var existing = context.getMappedInstance(domain, CourseEntity.class);
        if (existing != null) return existing;

        var entity = CourseEntity.builder()
                .courseId(EntityId.of(UUID.fromString(domain.getId().asString())))
                .title(domain.getTitle())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .status(domain.getStatus())
                .externalUserId(domain.getExternalUserId())
                .build();

        context.storeMappedInstance(domain, entity);

        var sectionEntities = SectionEntityMapper.mapToEntities(domain.getSections(), context);
        for (SectionEntity sectionEntity : sectionEntities) {
            sectionEntity.setCourse(entity);
        }

        entity.setSections(sectionEntities);

        return entity;
    }

    public static List<Course> mapToDomain(
            List<CourseEntity> entities,
            CycleAvoidingMappingContext context
    ) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(entity -> mapToDomain(entity, context))
                .toList();
    }

     /* ============================================================
       LIGHT MAPPING – FOR INIT VIDEO / READ-ONLY USE CASES
       ============================================================ */

    /**
     * Lightweight mapping:
     * - maps only identity + minimal fields
     * - DOES NOT map sections
     * - safe for init-video ownership validation
     */
    public static Course mapToDomainLight(CourseEntity entity, CycleAvoidingMappingContext context) {
        if (entity == null) return null;

        var existing = context.getMappedInstance(entity, Course.class);
        if (existing != null) return existing;

        var domain = Course.builder()
                .id(Id.of(entity.getId().value().toString()))
                .status(entity.getStatus())
                .title(entity.getTitle())
                .externalUserId(entity.getExternalUserId())
                .build();

        context.storeMappedInstance(entity, domain);

        return domain;
    }
}
