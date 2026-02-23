package com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo;

import com.simplon_project.skillhub.skillhub.storage.application.port.out.ObjectPutResult;

import java.io.InputStream;

public interface UploadStoragePort {
    ObjectPutResult upload(String bucket,
                           String key,
                           InputStream stream,
                           long size,
                           String contentType);
}
