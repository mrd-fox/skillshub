package com.simplon_project.skillhub.skillhub.storage.adapter.out.messaging.events;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoMetadata;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;

public record VideoMetadataExtractedEvent(
        String eventId,
        String eventVersion,
        Instant occurredAt,
        String producer,
        String courseId,
        String chapterId,
        String videoId,
        Long duration,
        Integer width,
        Integer height,
        String status,
        Instant uploadedAt

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static VideoMetadataExtractedEvent fromMediaContent(MediaContent media, VideoMetadata videoMetadata) {
        return new VideoMetadataExtractedEvent(
                EventId.random().asString(),
                "1",
                Instant.now(),
                "storage-ms",
                media.getCourseId(),
                media.getChapterId(),
                media.getId().asString(),
                videoMetadata.duration(),
                videoMetadata.width(),
                videoMetadata.height(),
                VideoStatusEnum.READY.name(),
                media.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
        );
    }
}
