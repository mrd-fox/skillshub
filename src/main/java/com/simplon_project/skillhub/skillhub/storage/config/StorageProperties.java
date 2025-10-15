package com.simplon_project.skillhub.skillhub.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("storage")
public record StorageProperties(
        String bucket,
        String prefix,
        int presignTtlSeconds,
        @Value("${storage.tempDir:#{systemProperties['java.io.tmpdir']}}")
        String tempDir
) {
    public StorageProperties {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("storage.bucket must not be blank.");
        }
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("storage.prefix must not be blank.");
        }
        if (presignTtlSeconds <= 0) {
            presignTtlSeconds = 900;
        }
    }
}