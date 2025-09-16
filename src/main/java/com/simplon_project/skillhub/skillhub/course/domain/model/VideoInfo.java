package com.simplon_project.skillhub.skillhub.course.domain.model;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;

import java.time.Duration;

public record VideoInfo(
        Id id,
        String key,  //key in external db
        Duration duration,
        String format,
        long size,
        int width,
        int height,
        VideoStatusEnum status
) {
}
