package com.simplon_project.skillhub.skillhub.course.adapter.messaging.listeners;

import com.simplon_project.skillhub.skillhub.course.adapter.messaging.events.VideoMetadataExtractedEvent;
import com.simplon_project.skillhub.skillhub.course.application.port.in.ProcessExtractedMetadataPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoMetadataExtractListener {
    private final ProcessExtractedMetadataPort processExtractedMetadataPort;

    @RabbitListener(
            queues = "${course.rabbitmq.metadata-queue}",
            containerFactory = "courseRabbitListenerContainerFactory"
    )
    public void handle(VideoMetadataExtractedEvent event) {
        log.info("📩 Received 'video.metadata.extracted' event for videoId={}", event.videoId());
        try {
            processExtractedMetadataPort.processVideoMetadata(event);
            log.info("✅ Video metadata updated successfully for videoId={}", event.videoId());
        } catch (Exception e) {
            log.error("❌ Failed to update video metadata for videoId={}: {}", event.videoId(), e.getMessage(), e);
        }
    }
}
