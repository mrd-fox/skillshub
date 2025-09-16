package com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo;

public interface DeleteMediaContentFromStoragePort {
    void delete(String bucket, String key);
}
