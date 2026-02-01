package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

/**
 * Outbound port.
 * One responsibility: trigger server-side polling/synchronization for a video
 * after upload confirmation (PROCESSING -> READY/FAILED).
 * <p>
 * One method.
 */
public interface StartVideoPollingPort {

    void start(VideoInfo videoInfo);
}