package com.simplon_project.skillhub.skillhub.storage.config.rabbit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "storage.rabbitmq")
@Component
@Getter
@Setter
public class RabbitStorageProps {
    private String exchange;
    private String uploadRoutingKey;
    private String metadataRoutingKey;
    private String uploadedQueue;
    private String metadataQueue;
}

