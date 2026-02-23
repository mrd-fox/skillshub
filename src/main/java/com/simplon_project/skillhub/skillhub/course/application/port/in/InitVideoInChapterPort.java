package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInit;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitVideoCommand;

public interface InitVideoInChapterPort {
    /**
     * Initialize a video upload for a chapter.
     * - Creates a Video in PENDING state
     * - Associates it to the chapter
     * - Initializes the external provider (e.g. Vimeo)
     * - Returns upload information to the frontend
     */
    VideoUploadInit init(InitVideoCommand command);
}
