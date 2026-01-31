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
        String embedHash,          // can be null (used for unlisted Vimeo videos)
        String errorMessage,       // can be null
        VideoStatusEnum status     // required
) {
    public VideoInfo markProcessing() {

        if (this.status != VideoStatusEnum.PENDING) {
            throw new IllegalStateException(
                    "Cannot confirm upload when status is " + status
            );
        }

        return VideoInfo.builder()
                .id(this.id)
                .sourceUri(this.sourceUri)
                .key(this.key)
                .duration(this.duration)
                .format(this.format)
                .size(this.size)
                .width(this.width)
                .height(this.height)
                .thumbnailUrl(this.thumbnailUrl)
                .embedHash(this.embedHash)
                .errorMessage(null)
                .status(VideoStatusEnum.PROCESSING)
                .build();
    }

    /**
     * Provider sync success: PROCESSING -> READY.
     * Fills metadata when available.
     */
    public VideoInfo markReady(
            String thumbnailUrl,
            Long duration,
            Integer width,
            Integer height,
            String format,
            Long size,
            String embedHash
    ) {
        requireStatus(VideoStatusEnum.PROCESSING, "mark ready");
        requireValidSourceUri(this.sourceUri);

        return VideoInfo.builder()
                .id(this.id)
                .sourceUri(this.sourceUri)
                .key(this.key)
                .duration(duration != null ? duration : this.duration)
                .format(format != null ? format : this.format)
                .size(size != null ? size : this.size)
                .width(width != null ? width : this.width)
                .height(height != null ? height : this.height)
                .thumbnailUrl(thumbnailUrl != null ? thumbnailUrl : this.thumbnailUrl)
                .embedHash(embedHash != null ? embedHash : this.embedHash)
                .errorMessage(null)
                .status(VideoStatusEnum.READY)
                .build();
    }

    /**
     * Provider sync failure: PROCESSING -> FAILED.
     */
    public VideoInfo markFailed(String errorMessage) {
        requireStatus(VideoStatusEnum.PROCESSING, "mark failed");
        requireValidSourceUri(this.sourceUri);

        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("errorMessage must not be blank");
        }

        return VideoInfo.builder()
                .id(this.id)
                .sourceUri(this.sourceUri)
                .key(this.key)
                .duration(this.duration)
                .format(this.format)
                .size(this.size)
                .width(this.width)
                .height(this.height)
                .thumbnailUrl(this.thumbnailUrl)
                .embedHash(this.embedHash)
                .errorMessage(errorMessage)
                .status(VideoStatusEnum.FAILED)
                .build();
    }

    /**
     * Logical expiration of an INIT window: PENDING -> EXPIRED.
     * (Requires EXPIRED to exist in the enum.)
     */
    public VideoInfo markExpired(String errorMessage) {
        requireStatus(VideoStatusEnum.PENDING, "mark expired");
        requireValidSourceUri(this.sourceUri);

        String finalError = errorMessage;
        if (finalError == null || finalError.isBlank()) {
            finalError = "Upload window expired";
        }

        return VideoInfo.builder()
                .id(this.id)
                .sourceUri(this.sourceUri)
                .key(this.key)
                .duration(this.duration)
                .format(this.format)
                .size(this.size)
                .width(this.width)
                .height(this.height)
                .thumbnailUrl(this.thumbnailUrl)
                .embedHash(this.embedHash)
                .errorMessage(finalError)
                .status(VideoStatusEnum.EXPIRED)
                .build();
    }

    // -----------------------------
    // Internal validation helpers
    // -----------------------------

    private void requireStatus(VideoStatusEnum expected, String operation) {
        if (this.status == null) {
            throw new IllegalStateException("Video status is missing");
        }

        if (this.status != expected) {
            throw new IllegalStateException(
                    "Cannot " + operation + " when status is " + this.status
            );
        }
    }

    private static void requireValidSourceUri(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("sourceUri must not be blank");
        }

        int schemeSeparatorIndex = value.indexOf("://");
        if (schemeSeparatorIndex <= 0) {
            throw new IllegalArgumentException("sourceUri must include a scheme (e.g. vimeo://123)");
        }

        String scheme = value.substring(0, schemeSeparatorIndex);
        if (scheme.isBlank()) {
            throw new IllegalArgumentException("sourceUri scheme must not be blank");
        }
    }

}
