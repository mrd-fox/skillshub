package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.InitVideoResponse;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInit;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class InitVideoResponseMapper {

    public static InitVideoResponse mapToResponse(VideoUploadInit init) {
        var video = init.video();
        var upload = init.upload();

        return InitVideoResponse.builder()
                .videoId(video.id().asString())
                .sourceUri(video.sourceUri())
                .thumbnailUrl(video.thumbnailUrl())
                .errorMessage(video.errorMessage())
                .status(video.status().name())
                .uploadProvider(upload.provider())
                .uploadUrl(upload.uploadUrl())
                .uploadExpiresAt(upload.expiresAt().toString())
                .build();
    }
}