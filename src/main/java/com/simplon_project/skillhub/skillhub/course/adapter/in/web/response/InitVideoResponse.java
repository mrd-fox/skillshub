package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import lombok.Builder;

@Builder
public record InitVideoResponse(
        // video (state persisted)
        String videoId,
        String sourceUri,
        String thumbnailUrl,
        String errorMessage,
        String status,

        // upload (orchestration, not persisted)
        String uploadProvider,
        String uploadUrl,
        String uploadExpiresAt
) {
}
