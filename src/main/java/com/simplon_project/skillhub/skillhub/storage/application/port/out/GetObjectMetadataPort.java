package com.simplon_project.skillhub.skillhub.storage.application.port.out;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.GetObjectMetadataCommand;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaMetadata;

public interface GetObjectMetadataPort {
    MediaMetadata get(GetObjectMetadataCommand command);
}
