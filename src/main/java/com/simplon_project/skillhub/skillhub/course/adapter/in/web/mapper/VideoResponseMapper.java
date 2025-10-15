package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.VideoResponse;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

public class VideoResponseMapper {
    public static VideoResponse mapToVideoResponse(VideoInfo video) {
        return new VideoResponse(
                video.id().asString(),
                video.key(),
                video.duration(),
                video.format(),
                video.size(),
                video.width(),
                video.height(),
                VideoStatusEnum.valueOf(video.status().name())
        );
    }
}
