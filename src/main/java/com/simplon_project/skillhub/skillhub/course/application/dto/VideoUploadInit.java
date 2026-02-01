package com.simplon_project.skillhub.skillhub.course.application.dto;

import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

import java.util.Objects;

public record VideoUploadInit(
        VideoInfo video,
        UploadInstructions upload
) {

    public VideoUploadInit {
        Objects.requireNonNull(video, "video must not be null");
        Objects.requireNonNull(upload, "upload must not be null");
    }


}
