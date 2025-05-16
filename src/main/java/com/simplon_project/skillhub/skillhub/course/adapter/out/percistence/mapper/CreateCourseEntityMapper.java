package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.GenericDtoMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.SectionEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CreateCourseEntityMapper extends GenericDtoMapper<Course, CourseEntity> {
    @Override
    @Mapping(target = "sections", source = "sections", qualifiedByName = "mapToEntitySections")
    CourseEntity toDto(Course domain, @Context CycleAvoidingMappingContext context);

    @Override
    @Mapping(target = "sections", source = "sections", qualifiedByName = "mapToDomainSections")
    Course toDomain(CourseEntity entity, @Context CycleAvoidingMappingContext context);


    @Named("mapToEntitySections")
    default List<SectionEntity> mapToEntitySections(List<Section> domainSections) {
        if (domainSections == null || domainSections.isEmpty()) {
            return new ArrayList<>();
        }

        return domainSections.stream()
                .map(domainSection -> {
                    var sectionEntity = SectionEntity.builder()
                            .title(domainSection.getTitle())
                            .chapters(mapToEntityChapters(domainSection.getChapters()))
                            .build();

                    // Associe la relation bidirectionnelle
                    sectionEntity.getChapters().forEach(ch -> ch.setSection(sectionEntity));
                    return sectionEntity;
                })
                .collect(Collectors.toList());
    }

    default List<ChapterEntity> mapToEntityChapters(List<Chapter> domainChapters) {
        if (domainChapters == null || domainChapters.isEmpty()) {
            return new ArrayList<>();
        }

        return domainChapters.stream()
                .map(domainChapter -> ChapterEntity.builder()
                        .title(domainChapter.getTitle())
                        .videoUrl(domainChapter.getVideoUrl())
                        .build())
                .collect(Collectors.toList());
    }


    @Named("mapToDomainSections")
    default List<Section> mapToDomainSections(List<SectionEntity> sectionEntities) {
        if (sectionEntities == null || sectionEntities.isEmpty()) {
            return new ArrayList<>();
        }

        return sectionEntities.stream()
                .map(sectionEntity -> {
                    var section = Section.builder()
                            .title(sectionEntity.getTitle())
                            .chapters(mapToDomainChapters(sectionEntity.getChapters()))
                            .build();

                    section.getChapters().forEach(ch -> ch.setSection(section));
                    return section;
                })
                .toList();
    }

    default List<Chapter> mapToDomainChapters(List<ChapterEntity> chapterEntities) {
        if (chapterEntities == null || chapterEntities.isEmpty()) {
            return new ArrayList<>();
        }
        return chapterEntities.stream()
                .map(chapterEntity -> {
                    return Chapter.builder()
                            .id(String.format(String.valueOf(chapterEntity.getId())))
                            .title(chapterEntity.getTitle())
                            .videoUrl(chapterEntity.getVideoUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
