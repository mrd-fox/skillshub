package com.simplon_project.skillhub.skillhub.storage.adapter.configminio;

import com.simplon_project.skillhub.skillhub.storage.adapter.Exceptions.StorageException;
import io.minio.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class MinioClientService {
    private final MinioClient minioClient;

    public MinioClientService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void upload(String bucket, String key, InputStream stream, long size, String contentType) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Erreur lors de l'upload dans MinIO: " + e.getMessage(), e);
        }
    }

    public InputStream download(String bucket, String key) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Erreur lors du téléchargement depuis MinIO: " + e.getMessage(), e);
        }
    }

    public void delete(String bucket, String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Erreur lors de la suppression dans MinIO: " + e.getMessage(), e);
        }
    }
}
