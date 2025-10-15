package com.simplon_project.skillhub.skillhub.storage.application.port.in.command;

public record GetObjectMetadataCommand(String objectKey) {
    public GetObjectMetadataCommand {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("objectKey must not be blank");
        }
    }
}
