package com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.mappers;


import com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.entity.MediaFileEntity;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;

public final class MediaContentMapper {
    public static MediaFileEntity mapToEntity(MediaContent mediaContent) {
        return MediaFileEntity.builder()
                .id(EntityId.fromString(mediaContent.getId().asString()))
                .filename(mediaContent.getFilename())
                .contentType(mediaContent.getContentType())
                .size(mediaContent.getSize())
                .uploaderId(mediaContent.getUploaderId())
                .courseId(mediaContent.getCourseId())
                .chapterId(mediaContent.getChapterId())
                .storagePath(mediaContent.getUrl())
                .createdAt(mediaContent.getCreatedAt())
                .build();
    }

    public static MediaContent mapToDomain(MediaFileEntity mediaFileEntity) {
        return MediaContent.builder()
                .id(MediaId.of(mediaFileEntity.getId().toString()))
                .uploaderId(mediaFileEntity.getUploaderId())
                .courseId(mediaFileEntity.getCourseId())
                .chapterId(mediaFileEntity.getChapterId())
                .filename(mediaFileEntity.getFilename())
                .contentType(mediaFileEntity.getContentType())
                .size(mediaFileEntity.getSize())
                .createdAt(mediaFileEntity.getCreatedAt())
                .url(mediaFileEntity.getStoragePath())
                .build();
    }
}
