package com.simplon_project.skillhub.skillhub.storage.application.port.out;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.StorageObjectMetadata;

public interface StatObjectPort {
    StorageObjectMetadata stat(String bucket, String key);
}
