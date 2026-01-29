package com.simplon_project.skillhub.skillhub.common.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitCommonConnectionProps implements RabbitConnectionProps {
    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
    private int connectionTimeout;
}
