package com.simplon_project.skillhub.skillhub.course.application.dto;

import java.time.Instant;

public record VideoUploadInitResult(
        String providerVideoId,
        String uploadUrl,
        Instant expiresAt,
        String sourceUri
) {
}