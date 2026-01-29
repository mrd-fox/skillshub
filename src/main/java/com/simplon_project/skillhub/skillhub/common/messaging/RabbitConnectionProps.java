package com.simplon_project.skillhub.skillhub.common.messaging;

public interface RabbitConnectionProps {
    String getHost();

    int getPort();

    String getUsername();

    String getPassword();

    String getVirtualHost();

    int getConnectionTimeout();
}
