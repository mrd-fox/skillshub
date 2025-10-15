package com.simplon_project.skillhub.skillhub.storage.application.port.in;

public interface AccessMediaPort {
    String generateReadUrl(String storageKey, int ttlSeconds);
}
