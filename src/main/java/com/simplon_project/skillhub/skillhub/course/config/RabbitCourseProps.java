package com.simplon_project.skillhub.skillhub.course.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "course.rabbitmq")
@Component
@Getter
@Setter
public class RabbitCourseProps {
    // Exchange used by the course-service
    private String exchange;

    // Legacy queues/keys (storage-service events)
    private String uploadRoutingKey;
    private String metadataRoutingKey;
    private String uploadedQueue;
    private String metadataQueue;
}
