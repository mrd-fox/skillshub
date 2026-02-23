package com.simplon_project.skillhub.skillhub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;


@org.springframework.context.annotation.Configuration
public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    @Value("10020")
    private String serverPort;

    private final BuildProperties buildProperties;

    public Configuration(@Autowired(required = false) final BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        if (LOGGER.isInfoEnabled()) {
            String baseUrl = String.format("http://localhost:%s/", serverPort);
            LOGGER.info("\n=====================================================================\n\n    " +
//                    "Service: " + String.format("%s:%s", buildProperties.getName(), buildProperties.getVersion()) + "\n\n    " +
                    "Base Url: " + baseUrl + "\n\n    " +
                    "Swagger Url: " + baseUrl + "swagger-ui/index.html\n\n   " +
                    "Health Url: " + baseUrl + "actuator/health\n\n" +
                    "=====================================================================");
        }
    }
}
