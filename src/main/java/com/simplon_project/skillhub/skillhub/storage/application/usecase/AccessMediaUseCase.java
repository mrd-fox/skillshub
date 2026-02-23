package com.simplon_project.skillhub.skillhub.storage.application.usecase;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.AccessMediaPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.PresignedUrlPort;
import com.simplon_project.skillhub.skillhub.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class AccessMediaUseCase implements AccessMediaPort {

    private final PresignedUrlPort presignedUrlPort;
    private final StorageProperties props;

    @Override
    public String generateReadUrl(String storageKey, int ttlSeconds) {
        int ttl = ttlSeconds > 0 ? ttlSeconds : props.presignTtlSeconds();
        return presignedUrlPort.generateGetUrl(props.bucket(), storageKey, ttl);
    }
}
