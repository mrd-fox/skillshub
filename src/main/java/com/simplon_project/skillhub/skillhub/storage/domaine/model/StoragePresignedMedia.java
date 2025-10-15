package com.simplon_project.skillhub.skillhub.storage.domaine.model;

import java.time.Instant;

public record StoragePresignedMedia(
        String mediaId,
        String storageKey,
        String presignedUrl,
        Instant expiresAt
) {
}
