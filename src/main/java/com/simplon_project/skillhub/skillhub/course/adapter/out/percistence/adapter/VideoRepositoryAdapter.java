package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.VideoInfoEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaVideoRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.VideoRepository;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional("courseTxManager")
public class VideoRepositoryAdapter implements VideoRepository {

    private final JpaVideoRepository jpaRepository;

    private final EntityManager entityManager;

    public VideoRepositoryAdapter(
            JpaVideoRepository jpaRepository,
            @Qualifier("courseEntityManager") EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<VideoEntity> findById(EntityId videoId) {
        return jpaRepository.findById(videoId);
    }

    @Override
    public boolean existsByChapterId(EntityId chapterId) {
        return jpaRepository.existsByChapterId(chapterId);
    }

    @Override
    public VideoInfo createPendingVideo(EntityId chapterId, String sourceUri, VideoStatusEnum status) {
        var chapterRef = entityManager.getReference(ChapterEntity.class, chapterId);

        var videoEntity = VideoEntity.builder()
                .videoId(EntityId.random())
                .sourceUri(sourceUri)
                .thumbnailUrl(null)
                .errorMessage(null)
                .status(status)
                .build();

        // Attach FK -> chapter
        videoEntity.setChapter(chapterRef);

        VideoEntity saved = jpaRepository.save(videoEntity);

        // Map Entity -> Domain (simple mapping here; ideally move to a mapper class)
        return VideoInfoEntityMapper.mapToDomain(saved, new CycleAvoidingMappingContext());


    }
}
