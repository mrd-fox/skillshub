package com.simplon_project.skillhub.skillhub.helpers.builders;


import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

import java.time.Instant;
import java.util.UUID;

public class VideoBuilder {

    private Id id = Id.of(UUID.randomUUID().toString());
    private String sourceUri = "vimeo://123456";
    private String key = "video-key";
    private Long duration = 120L;
    private String format = "mp4";
    private Long size = 1_000_000L;
    private Integer width = 1920;
    private Integer height = 1080;
    private String thumbnailUrl = "https://thumb.test/image.jpg";
    private String embedHash = "embed-hash";
    private String errorMessage = null;
    private VideoStatusEnum status = VideoStatusEnum.PROCESSING;
    private ExternalDeletionStatus externalDeletionStatus = ExternalDeletionStatus.NONE;
    private Instant deletedAt = null;

    private VideoBuilder() {
    }

    public static VideoBuilder aVideo() {
        return new VideoBuilder();
    }

    public VideoBuilder withId(String id) {
        this.id = Id.of(id);
        return this;
    }

    public VideoBuilder withStatus(VideoStatusEnum status) {
        this.status = status;
        return this;
    }

    public VideoBuilder withExternalDeletionStatus(ExternalDeletionStatus status) {
        this.externalDeletionStatus = status;
        return this;
    }

    public VideoBuilder withSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
        return this;
    }

    public VideoBuilder withoutSourceUri() {
        this.sourceUri = null;
        return this;
    }

    public VideoBuilder deletedNow() {
        this.deletedAt = Instant.now();
        return this;
    }

    public VideoBuilder deletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public VideoBuilder ready() {
        this.status = VideoStatusEnum.READY;
        return this;
    }

    public VideoBuilder processing() {
        this.status = VideoStatusEnum.PROCESSING;
        return this;
    }

    public VideoBuilder pending() {
        this.status = VideoStatusEnum.PENDING;
        return this;
    }

    public VideoBuilder withError(String message) {
        this.errorMessage = message;
        this.status = VideoStatusEnum.FAILED;
        return this;
    }

    public VideoInfo build() {
        return VideoInfo.builder()
                .id(id)
                .sourceUri(sourceUri)
                .key(key)
                .duration(duration)
                .format(format)
                .size(size)
                .width(width)
                .height(height)
                .thumbnailUrl(thumbnailUrl)
                .embedHash(embedHash)
                .errorMessage(errorMessage)
                .status(status)
                .externalDeletionStatus(externalDeletionStatus)
                .deletedAt(deletedAt)
                .build();
    }
}

