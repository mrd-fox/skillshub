package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;

import java.util.Optional;

public interface LoadVideoInfoByIdPort {
    Optional<VideoEntity> loadVideoInfoById(EntityId videoId);
}
