package com.simplon_project.skillhub.skillhub.course.application.port.out;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

import java.util.Optional;

public interface VideoRepository {

    Optional<VideoEntity> findById(EntityId videoId);

    boolean existsByChapterId(EntityId chapterId);

    VideoInfo createPendingVideo(EntityId chapterId, String sourceUri, VideoStatusEnum status);
}
