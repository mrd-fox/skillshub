package com.simplon_project.skillhub.skillhub.storage.adapter.out.storage;

import com.simplon_project.skillhub.skillhub.storage.adapter.configminio.MinioClientService;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.DeleteMediaContentFromStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.DownloadFromStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.UploadStoragePort;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class MinIOMediaContentAdapter implements UploadStoragePort, DownloadFromStoragePort, DeleteMediaContentFromStoragePort {

    private final MinioClientService minioClientService;

    public MinIOMediaContentAdapter(MinioClientService minioClientService) {
        this.minioClientService = minioClientService;
    }

    @Override
    public void upload(String bucket, String key, InputStream stream, long size, String contentType) {
        minioClientService.upload(bucket, key, stream, size, contentType);
    }

    @Override
    public InputStream download(String bucket, String key) {
        return minioClientService.download(bucket, key);
    }

    @Override
    public void delete(String bucket, String key) {
        minioClientService.delete(bucket, key);
    }
}
