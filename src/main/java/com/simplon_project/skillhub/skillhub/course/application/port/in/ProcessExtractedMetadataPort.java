package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.adapter.messaging.events.VideoMetadataExtractedEvent;

public interface ProcessExtractedMetadataPort {
    void processVideoMetadata(VideoMetadataExtractedEvent event);
}
