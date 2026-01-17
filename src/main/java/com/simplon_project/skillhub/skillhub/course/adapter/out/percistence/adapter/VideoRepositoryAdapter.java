package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.VideoInfoEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaVideoRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.chapter.CheckVideoExistsForChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.CreatePendingVideoForChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.LoadVideoInfoByIdPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.SaveVideoInfoPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional("courseTxManager")
public class VideoRepositoryAdapter implements
        CreatePendingVideoForChapterPort,
        LoadVideoInfoByIdPort,
        CheckVideoExistsForChapterPort,
        SaveVideoInfoPort {

    private final JpaVideoRepository jpaRepository;

    private final EntityManager entityManager;

    public VideoRepositoryAdapter(
            JpaVideoRepository jpaRepository,
            @Qualifier("courseEntityManager") EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<VideoInfo> loadVideoInfoById(String videoId) {
        return jpaRepository.findById(EntityId.fromString(videoId)).map(entity -> VideoInfoEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext()));
    }

    @Override
    public boolean checkVideoExistsForChapter(EntityId chapterId) {

        return jpaRepository.existsByChapterId(chapterId);
    }

    @Override
    public VideoInfo createPendingVideo(Id chapterId, String sourceUri) {
        if (chapterId == null) {
            throw new IllegalArgumentException("chapterId must not be null");
        }
        if (sourceUri == null || sourceUri.isBlank()) {
            throw new IllegalArgumentException("sourceUri must not be blank");
        }
        EntityId chapterEntityId = EntityId.of(chapterId.asUUID());
        var chapterRef = entityManager.getReference(ChapterEntity.class, chapterEntityId);

        var videoEntity = VideoEntity.builder()
                .videoId(EntityId.random())
                .sourceUri(sourceUri)
                .thumbnailUrl(null)
                .errorMessage(null)
                .status(VideoStatusEnum.PENDING)
                .build();

        // Attach FK -> chapter
        videoEntity.setChapter(chapterRef);

        VideoEntity saved = jpaRepository.save(videoEntity);

        // Map Entity -> Domain (simple mapping here; ideally move to a mapper class)
        return VideoInfoEntityMapper.mapToDomain(saved, new CycleAvoidingMappingContext());


    }

    @Override
    public VideoInfo save(VideoInfo videoInfo) {
        if (videoInfo == null) {
            throw new IllegalArgumentException("videoInfo must not be null");
        }
        if (videoInfo.id() == null) {
            throw new IllegalArgumentException("videoInfo.id must not be null");
        }
        if (videoInfo.status() == null) {
            throw new IllegalArgumentException("videoInfo.status must not be null");
        }

        var videoEntityId = EntityId.of(videoInfo.id().asUUID());

        var entity = jpaRepository.findById(videoEntityId)
                .orElseThrow(() -> new IllegalStateException("Video not found: " + videoInfo.id().asString()));

        // Persisted fields
        entity.setStorageKey(videoInfo.key()); // VideoInfo.key maps to VideoEntity.storageKey
        entity.setSourceUri(videoInfo.sourceUri());
        entity.setFormat(videoInfo.format());
        entity.setSize(videoInfo.size());
        entity.setWidth(videoInfo.width());
        entity.setHeight(videoInfo.height());
        entity.setDuration(videoInfo.duration());
        entity.setThumbnailUrl(videoInfo.thumbnailUrl());
        entity.setErrorMessage(videoInfo.errorMessage());
        entity.setStatus(videoInfo.status());

        VideoEntity saved = jpaRepository.save(entity);

        return VideoInfoEntityMapper.mapToDomain(saved, new CycleAvoidingMappingContext());
    }
}
