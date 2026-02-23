package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.ConfirmVideoUploadCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

public interface ConfirmVideoUploadPort {
    /**
     * Confirm that the upload is finished on the provider side.
     * - Sets status to PROCESSING
     * - Triggers server-side polling / sync to reach READY or FAILED
     * - Returns the updated VideoInfo
     */
    VideoInfo confirm(ConfirmVideoUploadCommand command);
}
