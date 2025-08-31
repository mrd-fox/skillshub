package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.List;
import java.util.UUID;

public class ChapterEntityMapper {
    public static Chapter mapToDomain(ChapterEntity entity, CycleAvoidingMappingContext context) {
        if (entity == null) return null;

        var existing = context.getMappedInstance(entity, Chapter.class);
        if (existing != null) return existing;

        var domain = Chapter.builder()
                .id(Id.of(entity.getId().toString()))
                .title(entity.getTitle())
                .position(entity.getPosition())
                .build();

        context.storeMappedInstance(entity, domain);

        domain.setVideo(VideoInfoEntityMapper.mapToDomain(entity.getVideo(), context));
        return domain;
    }

    public static List<Chapter> mapToDomains(List<ChapterEntity> entities, CycleAvoidingMappingContext context) {
        return entities.stream()
                .map(e -> mapToDomain(e, context))
                .toList();
    }

    public static ChapterEntity mapToEntity(Chapter domain, CycleAvoidingMappingContext context) {
        if (domain == null) return null;
        var existing = context.getMappedInstance(domain, ChapterEntity.class);
        if (existing != null) return existing;

        var entity = ChapterEntity.builder()
                .id(UUID.fromString(domain.getId().toString()))
                .title(domain.getTitle())
                .build();

        context.storeMappedInstance(domain, entity);

        entity.setVideo(VideoInfoEntityMapper.mapToEntity(domain.getVideo(), context));
        return entity;
    }

    public static List<ChapterEntity> mapToEntities(List<Chapter> domains, CycleAvoidingMappingContext context) {
        return domains.stream()
                .map(d -> mapToEntity(d, context))
                .toList();
    }
}
