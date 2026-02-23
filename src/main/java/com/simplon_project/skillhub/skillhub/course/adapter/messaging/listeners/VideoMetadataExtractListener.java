package com.simplon_project.skillhub.skillhub.course.adapter.messaging.listeners;

//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class VideoMetadataExtractListener {
//    private final ProcessExtractedMetadataPort processExtractedMetadataPort;
//
//    @RabbitListener(
//            queues = "${course.rabbitmq.metadata-queue}",
//            containerFactory = "courseRabbitListenerContainerFactory"
//    )
//    public void handle(VideoMetadataExtractedEvent event) {
//        log.info("📩 Received 'video.metadata.extracted' event for videoId={}", event.videoId());
//        try {
//            processExtractedMetadataPort.processVideoMetadata(event);
//            log.info("✅ Video metadata updated successfully for videoId={}", event.videoId());
//        } catch (Exception e) {
//            log.error("❌ Failed to update video metadata for videoId={}: {}", event.videoId(), e.getMessage(), e);
//        }
//    }
//}
