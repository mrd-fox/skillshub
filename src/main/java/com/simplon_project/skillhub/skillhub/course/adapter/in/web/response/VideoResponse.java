package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;

import java.time.Duration;

public record VideoResponse(
        String id,
        String key,
//        LocalDateTime createdAt,
//        LocalDateTime updatedAt,
        Duration duration,
        String format,
        long size,
        int width,
        int height,
        //todo map to enum response
        VideoStatusEnum status
) {
}
