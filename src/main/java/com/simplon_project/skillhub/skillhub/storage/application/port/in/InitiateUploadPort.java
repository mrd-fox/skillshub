package com.simplon_project.skillhub.skillhub.storage.application.port.in;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.InitiateUploadCommand;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.StoragePresignedMedia;

public interface InitiateUploadPort {
    StoragePresignedMedia initiate(InitiateUploadCommand command);
}
