package com.simplon_project.skillhub.skillhub;

import com.simplon_project.skillhub.skillhub.course.config.RabbitCourseVideoDeletionProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(
        {
                RabbitCourseVideoDeletionProps.class
        })
@Slf4j
public class SkillhubApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillhubApplication.class, args);
        log.info("✅ Application up and running");
    }
}
