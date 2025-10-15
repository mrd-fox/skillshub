package com.simplon_project.skillhub.skillhub.storage.adapter.out.messaging.publishers;

import com.simplon_project.skillhub.skillhub.storage.adapter.out.messaging.events.VideoMetadataExtractedEvent;
import com.simplon_project.skillhub.skillhub.storage.adapter.out.messaging.events.VideoUploadedEvent;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.eventpublisher.EventPublisherPort;
import com.simplon_project.skillhub.skillhub.storage.config.rabbit.RabbitStorageProps;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MediaEventPublisher implements EventPublisherPort {


    private final RabbitTemplate rabbitTemplate;
    private final RabbitStorageProps rabbitStorageProps;

    public MediaEventPublisher(
            @Qualifier("storageRabbitTemplate") RabbitTemplate rabbitTemplate, RabbitStorageProps rabbitStorageProps) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitStorageProps = rabbitStorageProps;
    }

    @Override
    public void publishMediaUploaded(MediaContent mediaContent) {
        var event = VideoUploadedEvent.fromMedia(mediaContent);
        log.info("📤 Publishing video.uploaded event for mediaId={}", mediaContent.getId());
        rabbitTemplate.convertAndSend(
                rabbitStorageProps.getExchange(),
                rabbitStorageProps.getUploadRoutingKey(),
                event
        );
    }

    @Override
    public void publishMetadataExtracted(MediaContent mediaContent, VideoMetadata metadata) {
        var event = VideoMetadataExtractedEvent.fromMediaContent(mediaContent, metadata);
        log.info("📤 Publishing video.metadata.extracted event for mediaId={}", mediaContent.getId());
        rabbitTemplate.convertAndSend(
                rabbitStorageProps.getExchange(),
                rabbitStorageProps.getMetadataRoutingKey(),
                event
        );
    }
}
