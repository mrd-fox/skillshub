package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.VideoResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class VideoResponseMapper {

    public static VideoResponse mapVideoInfoToResponse(VideoInfo video) {
        if (video == null) {
            return null;
        }

        return new VideoResponse(
                video.id().asString(),
                video.sourceUri(),
                video.duration(),
                video.format(),
                video.size(),
                video.width(),
                video.height(),
                video.thumbnailUrl(),
                video.embedHash(),
                video.errorMessage(),
                video.status().name()
        );
    }
}
