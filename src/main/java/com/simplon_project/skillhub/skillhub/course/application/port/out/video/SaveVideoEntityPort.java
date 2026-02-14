package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

/**
 * Outbound port to persist deletion state updates.
 */
public interface SaveVideoEntityPort {

    void markDeleted(String videoId);

    void markFailed(String videoId, int attempt, String errorMessage);

    void markRetryScheduled(String videoId, int attempt, String errorMessage);
}