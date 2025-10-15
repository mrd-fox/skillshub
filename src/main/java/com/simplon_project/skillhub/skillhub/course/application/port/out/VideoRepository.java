package com.simplon_project.skillhub.skillhub.course.application.port.out;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;

import java.util.Optional;

public interface VideoRepository {
    VideoEntity save(VideoEntity video);

    Optional<VideoEntity> findById(EntityId videoId);

    boolean existsByChapterId(EntityId chapterId);
}
