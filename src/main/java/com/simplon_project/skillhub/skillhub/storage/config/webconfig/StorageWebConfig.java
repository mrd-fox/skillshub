package com.simplon_project.skillhub.skillhub.storage.config.webconfig;

import com.simplon_project.skillhub.skillhub.storage.config.resolver.UploaderIdResolver;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@AllArgsConstructor
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class StorageWebConfig implements WebMvcConfigurer {
    private final UploaderIdResolver uploaderIdResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(uploaderIdResolver);
    }
}
