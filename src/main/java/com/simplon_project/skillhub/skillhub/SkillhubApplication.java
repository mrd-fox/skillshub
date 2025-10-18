package com.simplon_project.skillhub.skillhub;

import com.simplon_project.skillhub.skillhub.storage.config.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
@Slf4j
public class SkillhubApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillhubApplication.class, args);
        log.info("✅ Application démarrée — test création du fichier de log");
    }
}
