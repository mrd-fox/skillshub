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
     * Operational feature flag.
     * When false: dispatcher + listener + queue declarations are disabled so the app can start cleanly.
     */
    private boolean enabled = true;

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
     * Unified max retries for the whole deletion pipeline:
     * - Outbox publish attempts (DB -> Rabbit)
     * - Provider deletion attempts (Rabbit -> Vimeo)
     * <p>
     * Default: 5
     */
    private int maxRetryAttempts = 5;

    /**
     * Outbox dispatcher fixed delay in milliseconds.
     * How often the dispatcher polls for PENDING outbox events.
     * Default: 5000ms (5 seconds)
     */
    private long dispatcherFixedDelayMs = 5000L;

    /**
     * Outbox dispatcher batch size.
     * Maximum number of PENDING events processed per scheduler iteration.
     * Default: 10
     */
    private int dispatcherBatchSize = 10;
}