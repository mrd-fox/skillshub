package com.simplon_project.skillhub.skillhub.storage.application.event;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoMetadata;

import java.util.UUID;

public record VideoMetadataExtractedEvent(
        UUID mediaId,
        UUID courseId,
        UUID chapterId,
        VideoMetadata metadata,
        String status
) {
}
