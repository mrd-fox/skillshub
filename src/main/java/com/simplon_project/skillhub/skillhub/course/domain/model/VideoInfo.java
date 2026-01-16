package com.simplon_project.skillhub.skillhub.course.domain.model;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import lombok.Builder;

@Builder
public record VideoInfo(
        Id id,
        String sourceUri,          // canonical: vimeo://{id} or s3://...
        String key,                // optional legacy storage_key (can be null)
        Long duration,             // can be null until READY
        String format,             // can be null
        Long size,                 // can be null
        Integer width,             // can be null
        Integer height,            // can be null
        String thumbnailUrl,       // can be null
        String errorMessage,       // can be null
        VideoStatusEnum status     // required
) {
}
