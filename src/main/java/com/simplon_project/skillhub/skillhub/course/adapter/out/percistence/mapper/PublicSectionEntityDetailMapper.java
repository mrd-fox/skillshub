package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.SectionEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicSection;

import java.util.Comparator;
import java.util.List;

public final class PublicSectionEntityDetailMapper {

    private PublicSectionEntityDetailMapper() {
    }

    public static PublicSection mapToDomain(SectionEntity sectionEntity) {
        var chapters = PublicChapterEntityDetailMapper
                .mapToDomains(sectionEntity.getChapters().stream().toList());

        return PublicSection.of(
                Id.of(sectionEntity.getSectionId().toString()),
                sectionEntity.getTitle(),
                sectionEntity.getPosition(),
                chapters
        );
    }

    public static List<PublicSection> mapToDomains(List<SectionEntity> entities) {
        return entities.stream()
                .sorted(Comparator.comparingInt(SectionEntity::getPosition))
                .map(PublicSectionEntityDetailMapper::mapToDomain)
                .toList();
    }
}
