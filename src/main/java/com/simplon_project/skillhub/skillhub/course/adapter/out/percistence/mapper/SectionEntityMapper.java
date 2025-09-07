package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.SectionEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

import java.util.List;
import java.util.UUID;

public class SectionEntityMapper {
    public static Section mapToDomain(SectionEntity entity, CycleAvoidingMappingContext context) {
        if (entity == null) return null;

        Section existing = context.getMappedInstance(entity, Section.class);
        if (existing != null) return existing;

        var domain = Section.builder()
                .id(Id.of(entity.getId().toString()))
                .title(entity.getTitle())
                .build();

        context.storeMappedInstance(entity, domain);

        domain.setChapters(ChapterEntityMapper.mapToDomains(entity.getChapters(), context));
        return domain;
    }

    public static List<Section> mapToDomains(List<SectionEntity> entities, CycleAvoidingMappingContext context) {

        return entities.stream()
                .map(e -> mapToDomain(e, context))
                .toList();
    }

    public static SectionEntity mapToEntity(Section domain, CycleAvoidingMappingContext context) {
        if (domain == null) return null;
        var existing = context.getMappedInstance(domain, SectionEntity.class);

        if (existing != null) return existing;
        var entity = SectionEntity.builder()
                .id(UUID.fromString(domain.getId().asString()))
                .title(domain.getTitle())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        context.storeMappedInstance(domain, entity);

        var domainChapters = domain.getChapters() != null ? domain.getChapters() : List.<Chapter>of();
        var chapterEntities = ChapterEntityMapper.mapToEntities(domainChapters, context);

        for (ChapterEntity chapterEntity : chapterEntities) {
            chapterEntity.setSection(entity);
        }
        entity.setChapters(chapterEntities);
        return entity;
    }

    public static List<SectionEntity> mapToEntities(List<Section> domains, CycleAvoidingMappingContext context) {
        return domains.stream()
                .map(d -> mapToEntity(d, context))
                .toList();
    }


}
