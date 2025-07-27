package com.simplon_project.skillhub.skillhub.storage.application.port;

import java.io.InputStream;

public interface UploadStoragePort {
    void upload(String bucket, String key, InputStream stream, long size, String contentType);

}
