package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaVideoRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VideoRepositoryAdapter implements VideoRepository {

    private final JpaVideoRepository jpaRepository;


    @Override
    public VideoEntity save(VideoEntity video) {
        return jpaRepository.save(video);
    }

    @Override
    public Optional<VideoEntity> findById(EntityId videoId) {
        return jpaRepository.findById(videoId);
    }

    @Override
    public boolean existsByChapterId(EntityId chapterId) {
        return jpaRepository.existsByChapterId(chapterId);
    }
}
