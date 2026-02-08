package com.simplon_project.skillhub.skillhub.course.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for video deletion worker.
 * Manages RabbitMQ queues for asynchronous external video deletion.
 */
@ConfigurationProperties(prefix = "course.rabbitmq.video-deletion")
@Getter
@Setter
public class RabbitCourseVideoDeletionProps {

    /**
     * Feature flag to enable/disable deletion listeners and queue declarations.
     */
    private boolean enabled = true; // Enabled by default (production feature)

    /**
     * Main queue that processes video deletion requests.
     * Example: course.video.deletion.queue
     */
    private String deletionQueue;

    /**
     * Routing key used to publish deletion requests to the main queue.
     * Example: course.video.deletion.requested
     */
    private String deletionRoutingKey;

    /**
     * Delay queue used for retry backoff (TTL + DLX).
     * Example: course.video.deletion.delay.queue
     */
    private String deletionDelayQueue;

    /**
     * Routing key used to publish messages into the delay queue.
     * Example: course.video.deletion.delay
     */
    private String deletionDelayRoutingKey;

    /**
     * Maximum number of retry attempts before marking as FAILED.
     * Default: 5
     */
    private int maxRetryAttempts = 5;
}
