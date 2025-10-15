package com.simplon_project.skillhub.skillhub.course.adapter.messaging.listeners;

import com.simplon_project.skillhub.skillhub.course.adapter.messaging.events.VideoUploadedEvent;
import com.simplon_project.skillhub.skillhub.course.application.port.in.ProcessUploadVideoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoUploadedListener {
    private final ProcessUploadVideoPort processUploadVideoPort;

    @RabbitListener(
            queues = "${course.rabbitmq.uploaded-queue}",
            containerFactory = "courseRabbitListenerContainerFactory"
    )
    public void handle(VideoUploadedEvent event) {
        log.info("📩 Received 'video.uploaded' event for videoId={} courseId={}", event.videoId(), event.courseId());
        try {
            processUploadVideoPort.processUploadedVideo(event);
            log.info("✅ Video metadata updated successfully for videoId={}", event.videoId());
        } catch (Exception e) {
            log.error("❌ Failed to process video.uploaded event: {}", e.getMessage(), e);

        }
    }
}
