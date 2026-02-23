package com.simplon_project.skillhub.skillhub.storage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class StorageConfig {
}
