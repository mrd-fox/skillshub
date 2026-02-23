package com.simplon_project.skillhub.skillhub.user.application.port.in.dto;

import com.simplon_project.skillhub.skillhub.user.domain.model.User;

import java.util.List;
import java.util.UUID;

public record GetUserByExternalIdResult(
        User user,
        List<UUID> enrolledCourseIds
) {
}
