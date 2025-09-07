package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
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
                .build();
        context.storeMappedInstance(entity, domain);

        domain.setSections(SectionEntityMapper.mapToDomains(entity.getSections(), context));
        return domain;
    }


    public static CourseEntity mapToEntity(Course domain, CycleAvoidingMappingContext context) {
        var existing = context.getMappedInstance(domain, CourseEntity.class);
        if (existing != null) return existing;

        var entity = CourseEntity.builder()
                .id(UUID.fromString(domain.getId().asString()))
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


//    default List<SectionEntity> mapToEntitySections(List<Section> domainSections) {
//        if (domainSections == null || domainSections.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        return domainSections.stream()
//                .map(domainSection -> {
//                    var sectionEntity = SectionEntity.builder()
//                            .title(domainSection.getTitle())
//                            .chapters(mapToEntityChapters(domainSection.getChapters()))
//                            .build();
//
//                    // Associe la relation bidirectionnelle
//                    sectionEntity.getChapters().forEach(ch -> ch.setSection(sectionEntity));
//                    return sectionEntity;
//                })
//                .collect(Collectors.toList());
//    }
//
//    default List<ChapterEntity> mapToEntityChapters(List<Chapter> domainChapters) {
//        if (domainChapters == null || domainChapters.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        return domainChapters.stream()
//                .map(domainChapter -> ChapterEntity.builder()
//                        .title(domainChapter.getTitle())
//                        .videoUrl(domainChapter.getVideoUrl())
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//
//    default List<Section> mapToDomainSections(List<SectionEntity> sectionEntities) {
//        if (sectionEntities == null || sectionEntities.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        return sectionEntities.stream()
//                .map(sectionEntity -> {
//                    var section = Section.builder()
//                            .title(sectionEntity.getTitle())
//                            .chapters(mapToDomainChapters(sectionEntity.getChapters()))
//                            .build();
//
//                    section.getChapters().forEach(ch -> ch.setSection(section));
//                    return section;
//                })
//                .toList();
//    }
//
//    default List<Chapter> mapToDomainChapters(List<ChapterEntity> chapterEntities) {
//        if (chapterEntities == null || chapterEntities.isEmpty()) {
//            return new ArrayList<>();
//        }
//        return chapterEntities.stream()
//                .map(chapterEntity -> {
//                    return Chapter.builder()
//                            .id(String.format(String.valueOf(chapterEntity.getId())))
//                            .title(chapterEntity.getTitle())
//                            .videoUrl(chapterEntity.getVideoUrl())
//                            .build();
//                })
//                .collect(Collectors.toList());
//    }

}
