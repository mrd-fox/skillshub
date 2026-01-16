package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.SectionEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SectionEntityMapper {
    public static Section mapToDomain(SectionEntity entity, CycleAvoidingMappingContext context) {
        if (entity == null) return null;

        Section existing = context.getMappedInstance(entity, Section.class);
        if (existing != null) return existing;

        var domain = Section.builder()
                .id(Id.of(entity.getId().toString()))
                .title(entity.getTitle())
                .position(entity.getPosition())
                .build();

        context.storeMappedInstance(entity, domain);

        domain.setChapters(ChapterEntityMapper.mapToDomains(entity.getChapters(), context));
        return domain;
    }


    public static Set<Section> mapToDomains(Set<SectionEntity> entities, CycleAvoidingMappingContext context) {

        return entities.stream()
                .map(e -> mapToDomain(e, context))
                .collect(Collectors.toSet());
    }

    public static SectionEntity mapToEntity(Section domain, CycleAvoidingMappingContext context) {
        if (domain == null) return null;
        var existing = context.getMappedInstance(domain, SectionEntity.class);

        if (existing != null) return existing;
        var entity = SectionEntity.builder()
                .sectionId(EntityId.of(UUID.fromString(domain.getId().asString())))
                .title(domain.getTitle())
                .position(domain.getPosition())
                .build();

        context.storeMappedInstance(domain, entity);

        var domainChapters = domain.getChapters() != null ? domain.getChapters() : new HashSet<Chapter>();
        var chapterEntities = ChapterEntityMapper.mapToEntities(domainChapters, context);

        for (ChapterEntity chapterEntity : chapterEntities) {
            chapterEntity.setSection(entity);
        }
        entity.setChapters(chapterEntities);
        return entity;
    }

    public static Set<SectionEntity> mapToEntities(Set<Section> domains, CycleAvoidingMappingContext context) {
        return domains.stream()
                .map(d -> mapToEntity(d, context))
                .collect(Collectors.toSet());
    }


    /**
     * Dedicated lightweight mapping (no deep graph):
     * - maps only section fields + course (light)
     * - does NOT map chapters to avoid cycles and unnecessary loading
     */
    public static Section mapToDomainLight(SectionEntity entity, CycleAvoidingMappingContext context) {
        if (entity == null) return null;

        Section existing = context.getMappedInstance(entity, Section.class);
        if (existing != null) return existing;

        var domain = Section.builder()
                .id(Id.of(entity.getId().toString()))
                .title(entity.getTitle())
                .position(entity.getPosition())
                .build();

        context.storeMappedInstance(entity, domain);

        // Course is required for init-video validation (courseId ownership)
        domain.setCourse(CourseEntityMapper.mapToDomainLight(entity.getCourse(), context));

        return domain;
    }


}
