package com.simplon_project.skillhub.skillhub.storage.application.usecase;

import com.simplon_project.skillhub.skillhub.storage.adapter.MinIOStorageAdapter;
import com.simplon_project.skillhub.skillhub.storage.application.port.DeleteStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.DownloadStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.UploadStoragePort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Primary
public class StorageUseCases implements UploadStoragePort, DownloadStoragePort, DeleteStoragePort {
    private final MinIOStorageAdapter minIOStorageAdapter;

    public StorageUseCases(MinIOStorageAdapter minIOStorageAdapter) {
        this.minIOStorageAdapter = minIOStorageAdapter;
    }


    @Override
    public void delete(String bucket, String key) {
    }

    @Override
    public InputStream download(String bucket, String key) {
        return null;
    }

    @Override
    public void upload(String bucket, String key, InputStream stream, long size, String contentType) {
        minIOStorageAdapter.upload(bucket, key, stream, size, contentType);
    }
}
