package com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo;

import java.io.InputStream;

public interface UploadToStorageMinIoPort {
    void upload(String bucket, String key, InputStream stream, long size, String contentType);
}
//todo to delete?
