package com.simplon_project.skillhub.skillhub.storage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {
    @Bean
    public Clock clock() {
        // UTC recommandé pour la cohérence des timestamps
        return Clock.systemUTC();
    }
}
