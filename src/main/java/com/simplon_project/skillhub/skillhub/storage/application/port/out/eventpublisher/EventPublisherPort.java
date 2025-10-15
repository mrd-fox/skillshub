package com.simplon_project.skillhub.skillhub.storage.application.port.out.eventpublisher;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoMetadata;

public interface EventPublisherPort {
    void publishMediaUploaded(MediaContent mediaContent);

    void publishMetadataExtracted(MediaContent mediaContent, VideoMetadata metadata);


}
