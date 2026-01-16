package com.simplon_project.skillhub.skillhub.course.adapter.out.external.vimeo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class VimeoWebClientConfig {

    @Bean
    public WebClient vimeoWebClient(
            @Value("${vimeo.base-url}") String baseUrl,
            @Value("${vimeo.accept}") String accept
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, accept)
                .build();
    }
}