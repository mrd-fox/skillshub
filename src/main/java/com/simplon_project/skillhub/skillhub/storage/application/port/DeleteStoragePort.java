package com.simplon_project.skillhub.skillhub.storage.application.port;

public interface DeleteStoragePort {
    void delete(String bucket, String key);
}
