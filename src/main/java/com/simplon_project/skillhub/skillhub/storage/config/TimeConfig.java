package com.simplon_project.skillhub.skillhub.storage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class TimeConfig {
    @Bean
    public Clock clock() {
        // UTC recommandé pour la cohérence des timestamps
        return Clock.systemUTC();
    }
}
