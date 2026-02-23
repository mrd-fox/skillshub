package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.SectionEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicChapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicSection;

public class PublicSectionCatalogMapper {

    private PublicSectionCatalogMapper() {
    }

    static PublicSection mapToDomain(
            SectionEntity sectionEntity
    ) {
        var chapters = sectionEntity.getChapters().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(ch -> PublicChapter.of(
                        Id.of(ch.getChapterId().toString()),
                        ch.getTitle(),
                        ch.getPosition()
                ))
                .toList();

        return PublicSection.of(
                Id.of(sectionEntity.getSectionId().toString()),
                sectionEntity.getTitle(),
                sectionEntity.getPosition(),
                chapters
        );
    }
}