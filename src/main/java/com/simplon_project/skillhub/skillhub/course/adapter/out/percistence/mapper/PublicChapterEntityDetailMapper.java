package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicChapter;

import java.util.Comparator;
import java.util.List;

public final class PublicChapterEntityDetailMapper {

    private PublicChapterEntityDetailMapper() {
    }

    public static PublicChapter mapToDomain(ChapterEntity entity) {
        return PublicChapter.of(
                Id.of(entity.getChapterId().toString()),
                entity.getTitle(),
                entity.getPosition()
        );
    }

    public static List<PublicChapter> mapToDomains(List<ChapterEntity> entities) {
        return entities.stream()
                .sorted(Comparator.comparingInt(ChapterEntity::getPosition))
                .map(PublicChapterEntityDetailMapper::mapToDomain)
                .toList();
    }
}