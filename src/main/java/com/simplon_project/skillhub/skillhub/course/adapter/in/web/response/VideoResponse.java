package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Video metadata and playback information")
public record VideoResponse(
        @Schema(description = "Video unique identifier", example = "5d8a3b1c-9e2f-4c7d-8a5b-1f3e6d9c2a4b")
        String id,

        @Schema(description = "Video source URI (Vimeo link)", example = "https://vimeo.com/123456789")
        String sourceUri,

        @Schema(description = "Video duration in seconds", example = "600")
        Long duration,

        @Schema(description = "Video format", example = "mp4")
        String format,

        @Schema(description = "Video file size in bytes", example = "104857600")
        Long size,

        @Schema(description = "Video width in pixels", example = "1920")
        Integer width,

        @Schema(description = "Video height in pixels", example = "1080")
        Integer height,

        @Schema(description = "Video thumbnail URL", example = "https://i.vimeocdn.com/video/123456789.jpg")
        String thumbnailUrl,

        @Schema(description = "Video embed hash for iframe", example = "abc123def456")
        String embedHash,

        @Schema(description = "Error message if video processing failed")
        String errorMessage,

        @Schema(description = "Video processing status", example = "READY")
        String status
) {
}
