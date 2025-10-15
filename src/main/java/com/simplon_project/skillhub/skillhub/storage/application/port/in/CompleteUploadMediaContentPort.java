package com.simplon_project.skillhub.skillhub.storage.application.port.in;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.CompleteUploadCommand;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;

public interface CompleteUploadMediaContentPort {
    MediaContent complete(CompleteUploadCommand command);
}
