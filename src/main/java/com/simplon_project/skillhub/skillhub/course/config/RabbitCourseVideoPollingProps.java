package com.simplon_project.skillhub.skillhub.course.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "course.rabbitmq.video-polling")
@Getter
@Setter
public class RabbitCourseVideoPollingProps {

    /**
     * Feature flag to enable/disable polling listeners and queue declarations.
     */
    private boolean enabled = false;

    /**
     * Main queue that triggers a polling attempt.
     * Example: course.video.polling.queue
     */
    private String pollingQueue;

    /**
     * Routing key used to publish polling requests to the main queue.
     * Example: course.video.polling.requested
     */
    private String pollingRoutingKey;

    /**
     * Delay queue used for backoff retries (TTL + DLX).
     * Example: course.video.polling.delay.queue
     */
    private String pollingDelayQueue;

    /**
     * Routing key used to publish messages into the delay queue.
     * Example: course.video.polling.delay
     */
    private String pollingDelayRoutingKey;

    /**
     * Max polling window in minutes (MVP default: 15).
     */
    private int timeoutMinutes = 15;
}