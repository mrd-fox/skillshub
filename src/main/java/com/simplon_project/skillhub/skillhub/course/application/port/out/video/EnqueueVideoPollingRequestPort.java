package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import java.time.Instant;

public interface EnqueueVideoPollingRequestPort {
    /**
     * @param videoId     domain video id as String (UUID string)
     * @param attempt     retry attempt (0 for first)
     * @param requestedAt first request timestamp (for global timeout)
     * @param delayMs     delay before processing (0 = immediate)
     */
    void enqueue(String videoId, int attempt, Instant requestedAt, long delayMs);
}
