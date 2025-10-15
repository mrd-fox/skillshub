package com.simplon_project.skillhub.skillhub.storage.adapter.out.messaging.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoMetadataStorageExtractListener {
    //todo to implement
//    @RabbitListener(
//            queues = "${storage.rabbitmq.metadata-request-queue}",
//            containerFactory = "storageRabbitListenerContainerFactory"
//    )
//    public void handleMetadataRequested(VideoMetadataRequestedEvent event) {
//        // lance l’analyse et publie ensuite `video.metadata.extracted`
//    }
}
