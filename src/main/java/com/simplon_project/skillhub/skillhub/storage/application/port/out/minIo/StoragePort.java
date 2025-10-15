package com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaMetadata;

public interface StoragePort {
    MediaMetadata head(String objectKey);
}
