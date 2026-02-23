package com.simplon_project.skillhub.skillhub.storage.domaine.model;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class StorageObjectMetadata {
    private final long size;
    private final String contentType;
    private final String etag;
    private final ZonedDateTime lastModified;
    private final String objectKey;
}
