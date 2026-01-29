package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

import java.util.UUID;

public class VideoInfoEntityMapper {
    public static VideoInfo mapToDomain(VideoEntity entity, CycleAvoidingMappingContext context) {
        if (entity == null) {
            return null;
        }
        var existing = context.getMappedInstance(entity, VideoInfo.class);
        if (existing != null) {
            return existing;
        }

        var domain = new VideoInfo(
                Id.of(entity.getId().toString()),
                entity.getSourceUri(),
                entity.getStorageKey(),
                entity.getDuration(),
                entity.getFormat(),
                entity.getSize(),
                entity.getWidth(),
                entity.getHeight(),
                entity.getThumbnailUrl(),
                entity.getErrorMessage(),
                entity.getStatus()
        );
        context.storeMappedInstance(entity, domain);
        return domain;
    }

    public static VideoEntity mapToEntity(VideoInfo domain, CycleAvoidingMappingContext context) {
        if (domain == null) {
            return null;
        }

        var existing = context.getMappedInstance(domain, VideoEntity.class);
        if (existing != null) {
            return existing;
        }

        UUID videoUuid = UUID.fromString(domain.id().asString());

        var entity = VideoEntity.builder()
                .videoId(EntityId.of(videoUuid))
                .storageKey(domain.key()) // optional
                .sourceUri(domain.sourceUri())
                .duration(domain.duration())
                .format(domain.format())
                .size(domain.size())
                .width(domain.width())
                .height(domain.height())
                .thumbnailUrl(domain.thumbnailUrl())
                .errorMessage(domain.errorMessage())
                .status(domain.status())
                .build();

        context.storeMappedInstance(domain, entity);
        return entity;

    }
}
