package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.config.helper.DateTimeHelper;
import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChapterEntityMapper {
    public static Chapter mapToDomain(ChapterEntity entity, CycleAvoidingMappingContext context) {
        if (entity == null) return null;

        var existing = context.getMappedInstance(entity, Chapter.class);
        if (existing != null) return existing;

        var zone = ZoneId.systemDefault();
        var domain = Chapter.builder()
                .id(Id.of(entity.getId().toString()))
                .title(entity.getTitle())
                .position(entity.getPosition())
                .createdAt(DateTimeHelper.toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(DateTimeHelper.toLocalDateTime(entity.getUpdatedAt()))
                .video(entity.getVideo() != null ? VideoInfoEntityMapper.mapToDomain(entity.getVideo(), context) : null)
                .build();
        context.storeMappedInstance(entity, domain);
        domain.setVideo(VideoInfoEntityMapper.mapToDomain(entity.getVideo(), context));
        return domain;
    }

    public static List<Chapter> mapToDomains(Set<ChapterEntity> entities, CycleAvoidingMappingContext context) {
        return entities.stream()
                .map(e -> mapToDomain(e, context))
                .toList();
    }

    public static ChapterEntity mapToEntity(Chapter domain, CycleAvoidingMappingContext context) {
        if (domain == null) return null;
        var existing = context.getMappedInstance(domain, ChapterEntity.class);
        if (existing != null) return existing;

        var entity = ChapterEntity.builder()
                .chapterId(EntityId.of(UUID.fromString(domain.getId().asString())))
                .title(domain.getTitle())
                .position(domain.getPosition())
                .build();

        context.storeMappedInstance(domain, entity);

        entity.setVideo(VideoInfoEntityMapper.mapToEntity(domain.getVideo(), context));
        return entity;
    }

    public static Set<ChapterEntity> mapToEntities(Set<Chapter> domains, CycleAvoidingMappingContext context) {
        return domains.stream()
                .map(d -> mapToEntity(d, context))
                .collect(Collectors.toSet());
    }
}
