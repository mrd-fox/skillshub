package com.simplon_project.skillhub.skillhub.storage.domaine.model;

import java.time.Instant;
import java.util.Map;


public record MediaMetadata(long sizeBytes,
                            String eTag,
                            String contentType,
                            Instant lastModified,
                            Map<String, String> userMetadata) {


}

