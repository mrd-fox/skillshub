package com.simplon_project.skillhub.skillhub.user.application.port.in.command;

import java.util.UUID;

public record GetUserByExternalIdCommand(String rawExternalId) {

    public GetUserByExternalIdCommand {
        if (rawExternalId == null || rawExternalId.isBlank()) {
            throw new IllegalArgumentException("External ID cannot be null or empty.");
        }

        try {
            UUID.fromString(rawExternalId);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("External ID must be a valid UUID.", ex);
        }
    }

    public UUID toExternalId() {
        return UUID.fromString(rawExternalId);
    }
}
