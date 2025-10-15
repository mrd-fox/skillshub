package com.simplon_project.skillhub.skillhub.storage.adapter.out.messaging.events;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoStatusEnum;

import java.io.Serializable;
import java.time.Instant;

public record VideoUploadedEvent(
        String eventId,
        String eventVersion,
        Instant occurredAt,
        String producer,
        String courseId,
        String chapterId,
        String videoId,
        String storageKey,
        String format,
        long sizeBytes,
        String status,
        Instant uploadedAt
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public static VideoUploadedEvent fromMedia(MediaContent media) {

        return new VideoUploadedEvent(
                EventId.random().asString(),
                "1",
                Instant.now(),
                "storage-ms",
                media.getCourseId(),
                media.getChapterId(),
                media.getId().asString(),
                media.getUrl(),
                media.getContentType(),
                media.getSize(),
                VideoStatusEnum.UPLOADED.name(),
                Instant.now()
        );
    }


}
