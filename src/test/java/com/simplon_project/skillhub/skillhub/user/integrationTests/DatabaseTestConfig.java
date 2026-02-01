package com.simplon_project.skillhub.skillhub.user.integrationTests;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class DatabaseTestConfig {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("skillshub_test")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {

        // Bind ALL datasources to the same Postgres for integration tests
        registry.add("spring.datasource.course.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.course.username", postgres::getUsername);
        registry.add("spring.datasource.course.password", postgres::getPassword);

        registry.add("spring.datasource.storage.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.storage.username", postgres::getUsername);
        registry.add("spring.datasource.storage.password", postgres::getPassword);

        registry.add("spring.datasource.user.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.user.username", postgres::getUsername);
        registry.add("spring.datasource.user.password", postgres::getPassword);

        // Disable Rabbit listeners in tests (avoids missing queue / broker issues)
        registry.add("course.rabbitmq.video-polling.enabled", () -> "false");
        registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
        registry.add("spring.rabbitmq.listener.direct.auto-startup", () -> "false");

        // If you have MinIO/Vimeo required properties, disable/neutralize
        registry.add("minio.enabled", () -> "false");
        registry.add("vimeo.enabled", () -> "false");
    }
}