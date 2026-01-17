package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInitResult;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitProviderUploadCommand;

public interface VideoProviderInitPort {
    VideoUploadInitResult initTusUpload(InitProviderUploadCommand command);
}
