package com.simplon_project.skillhub.skillhub.storage.adapter.out.storage;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.PresignedUrlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class S3PresignedUrlAdapter implements PresignedUrlPort {

    private final AmazonS3 amazonClient;

    @Override
    public String generatePutUrl(String bucket, String key, int ttlSeconds, String contentType) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, HttpMethod.PUT);
        request.setExpiration(Date.from(Instant.now().plusSeconds(ttlSeconds)));
        request.setContentType(contentType);
        return amazonClient.generatePresignedUrl(request).toString();
    }

    @Override
    public String generateGetUrl(String bucket, String key, int ttlSeconds) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, HttpMethod.GET);
        request.setExpiration(Date.from(Instant.now().plusSeconds(ttlSeconds)));
        return amazonClient.generatePresignedUrl(request).toString();
    }

}