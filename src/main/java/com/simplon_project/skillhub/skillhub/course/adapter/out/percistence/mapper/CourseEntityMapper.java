package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.config.helper.DateTimeHelper;
import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.SectionEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.UUID;


public class CourseEntityMapper {

    public static Course mapToDomain(CourseEntity entity, CycleAvoidingMappingContext context) {
        var existing = context.getMappedInstance(entity, Course.class);
        if (existing != null) return existing;

        var domain = Course.builder()
                .id(Id.of(entity.getId().toString()))
                .status(entity.getStatus())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .createdAt(DateTimeHelper.toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(DateTimeHelper.toLocalDateTime(entity.getUpdatedAt()))
                .build();
        context.storeMappedInstance(entity, domain);

        domain.setSections(SectionEntityMapper.mapToDomains(entity.getSections(), context));
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
                .build();

        context.storeMappedInstance(domain, entity);

        var sectionEntities = SectionEntityMapper.mapToEntities(domain.getSections(), context);
        for (SectionEntity sectionEntity : sectionEntities) {
            sectionEntity.setCourse(entity);
        }

        entity.setSections(sectionEntities);

        return entity;
    }
}
