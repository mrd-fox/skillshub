package com.simplon_project.skillhub.skillhub.storage.application.usecase;

import com.simplon_project.skillhub.skillhub.storage.adapter.out.storage.MinIOMediaContentAdapter;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.DeleteMediaContentFromStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.DownloadFromStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.UploadStoragePort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Primary
public class MediaContentUseCases implements UploadStoragePort, DownloadFromStoragePort, DeleteMediaContentFromStoragePort {
    private final MinIOMediaContentAdapter minIOStorageAdapter;

    public MediaContentUseCases(MinIOMediaContentAdapter minIOStorageAdapter) {
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
