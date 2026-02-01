package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

import java.util.Optional;

public interface LoadVideoInfoByIdPort {
    Optional<VideoInfo> loadVideoInfoById(String videoId);
}
