package com.simplon_project.skillhub.skillhub.user.application.port.in.command;

import com.simplon_project.skillhub.skillhub.user.domain.model.Id;

public record GetUserByIdCommand(String rawId) {

    public GetUserByIdCommand {

        if (rawId == null || rawId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        Id.of(rawId); // ensures UUID format
    }

    public Id toDomainId() {
        return Id.of(rawId);
    }
}
