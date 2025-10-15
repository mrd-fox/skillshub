package com.simplon_project.skillhub.skillhub.course.domain.model;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;

public record VideoInfo(
        Id id,
        String key,  //key in external db
        Long duration,
        String format,
        long size,
        int width,
        int height,
        VideoStatusEnum status
) {
}
