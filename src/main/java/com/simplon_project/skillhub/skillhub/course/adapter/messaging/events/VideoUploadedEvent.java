package com.simplon_project.skillhub.skillhub.course.adapter.messaging.events;

import java.time.Instant;

public record VideoUploadedEvent(
        String eventId,
        String producer,
        String courseId,
        String chapterId,
        String videoId,
        String storageKey,
        String format,
        long sizeBytes,
        String status,
        Instant uploadedAt
) {
}
