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
    private String exchange;
    private String uploadRoutingKey;
    private String metadataRoutingKey;
    private String uploadedQueue;
    private String metadataQueue;
}
