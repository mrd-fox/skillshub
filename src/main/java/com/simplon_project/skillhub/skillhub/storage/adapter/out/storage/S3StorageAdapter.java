package com.simplon_project.skillhub.skillhub.storage.adapter.out.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.DownloadStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.StatObjectPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.StoragePort;
import com.simplon_project.skillhub.skillhub.storage.config.StorageProperties;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaMetadata;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.StorageObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class S3StorageAdapter implements StoragePort, StatObjectPort, DownloadStoragePort {
    private final AmazonS3 amazonS3;
    private final StorageProperties props;


    @Override
    public MediaMetadata head(String objectKey) {
        try {
            final String key = normalizeKey(objectKey, props.prefix());
            ObjectMetadata metadata = amazonS3.getObjectMetadata(props.bucket(), key);

            Instant lastModified = metadata.getLastModified() != null
                    ? metadata.getLastModified().toInstant()
                    : null;

            return new MediaMetadata(
                    metadata.getContentLength(),
                    metadata.getETag(),
                    metadata.getContentType(),
                    lastModified,
                    metadata.getUserMetadata() != null ? metadata.getUserMetadata() : Map.of()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch object metadata from S3/MinIO", e);
        }
    }

    private String normalizeKey(String key, String prefix) {
        if (prefix == null || prefix.isBlank()) return key;
        return key.startsWith(prefix + "/") ? key : prefix + "/" + key;
    }

    @Override
    public StorageObjectMetadata stat(String bucket, String key) {
        try {
            ObjectMetadata metadata = amazonS3.getObjectMetadata(bucket, key);

            if (metadata == null) {
                throw new IllegalStateException("No metadata found for key: " + key);
            }

            return StorageObjectMetadata.builder()
                    .size(metadata.getContentLength())
                    .contentType(metadata.getContentType())
                    .etag(metadata.getETag())
                    .lastModified(
                            metadata.getLastModified() != null
                                    ? ZonedDateTime.ofInstant(metadata.getLastModified().toInstant(), ZoneId.systemDefault())
                                    : null
                    )
                    .objectKey(key)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve metadata for object " + key, e);
        }
    }

    @Override
    public Path download(String objectKey) {
        try {
            final String key = normalizeKey(objectKey, props.prefix());
            Path tempDir = Path.of(props.tempDir());
            Files.createDirectories(tempDir);

            Path tempFile = Files.createTempFile("media-", ".tmp");

            S3Object s3Object = amazonS3.getObject(props.bucket(), key);
            try (var inputStream = s3Object.getObjectContent()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return tempFile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download object from S3", e);
        }
    }
}
