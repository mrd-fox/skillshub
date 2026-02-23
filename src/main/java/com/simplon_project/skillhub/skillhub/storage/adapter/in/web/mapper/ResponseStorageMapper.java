package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.response.PresignedUploadResponse;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.StoragePresignedMedia;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ResponseStorageMapper {
    public static PresignedUploadResponse mapToResponse(StoragePresignedMedia presignedMedia) {
        return new PresignedUploadResponse(
                presignedMedia.mediaId(),
                presignedMedia.storageKey(),
                presignedMedia.presignedUrl(),
                presignedMedia.expiresAt()
        );
    }
}
