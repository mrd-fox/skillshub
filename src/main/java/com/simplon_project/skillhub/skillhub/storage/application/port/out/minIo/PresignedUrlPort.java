package com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo;

public interface PresignedUrlPort {
    String generatePutUrl(String bucket, String key, int ttlSeconds, String contentType);

    String generateGetUrl(String bucket, String key, int ttlSeconds);
}

