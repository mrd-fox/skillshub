package com.simplon_project.skillhub.skillhub.course.adapter.messaging.events;

import java.time.Instant;

public record VideoMetadataExtractedEvent(
        String eventId,
        String producer,
        String courseId,
        String chapterId,
        String videoId,
        Integer width,
        Integer height,
        Long duration,
        String status,
        Instant occurredAt
) {
}
