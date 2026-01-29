package com.simplon_project.skillhub.skillhub.course.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplon_project.skillhub.skillhub.common.messaging.RabbitConnectionProps;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({
        RabbitCourseProps.class,
        RabbitCourseVideoPollingProps.class
})
public class RabbitCourseConfig {

    private final RabbitConnectionProps connectionProps;
    private final RabbitCourseProps rabbitCourseProps;
    private final RabbitCourseVideoPollingProps pollingProps;
    private final ObjectMapper objectMapper;

    @Bean(name = "courseRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory courseRabbitListenerContainerFactory(
            @Qualifier("courseConnectionFactory") CachingConnectionFactory connectionFactory,
            @Qualifier("courseMessageConverter") Jackson2JsonMessageConverter messageConverter) {

        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean(name = "courseConnectionFactory")
    @Primary
    public CachingConnectionFactory connectionFactory() {
        var factory = new CachingConnectionFactory(connectionProps.getHost(), connectionProps.getPort());
        factory.setUsername(connectionProps.getUsername());
        factory.setPassword(connectionProps.getPassword());
        factory.setVirtualHost(connectionProps.getVirtualHost());
        factory.setConnectionTimeout(connectionProps.getConnectionTimeout());
        return factory;
    }

    @Bean(name = "courseExchange")
    public TopicExchange courseExchange() {
        return new TopicExchange(rabbitCourseProps.getExchange(), true, false);
    }

    @Bean(name = "courseUploadedQueue")
    public Queue courseUploadedQueue() {
        return new Queue(rabbitCourseProps.getUploadedQueue(), true);
    }

    @Bean(name = "courseMetadataQueue")
    public Queue courseMetadataQueue() {
        return new Queue(rabbitCourseProps.getMetadataQueue(), true);
    }

    @Bean
    public Binding courseUploadedBinding(
            @Qualifier("courseUploadedQueue") Queue uploadedQueue,
            @Qualifier("courseExchange") TopicExchange courseExchange) {
        return BindingBuilder.bind(uploadedQueue)
                .to(courseExchange)
                .with(rabbitCourseProps.getUploadRoutingKey());
    }

    @Bean
    public Binding courseMetadataBinding(
            @Qualifier("courseMetadataQueue") Queue metadataQueue,
            @Qualifier("courseExchange") TopicExchange courseExchange) {
        return BindingBuilder.bind(metadataQueue)
                .to(courseExchange)
                .with(rabbitCourseProps.getMetadataRoutingKey());
    }

    @Bean(name = "courseMessageConverter")
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean(name = "courseRabbitTemplate")
    public RabbitTemplate rabbitTemplate(
            @Qualifier("courseConnectionFactory") CachingConnectionFactory connectionFactory,
            @Qualifier("courseMessageConverter") Jackson2JsonMessageConverter messageConverter
    ) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(rabbitCourseProps.getExchange());
        return template;
    }
    // -----------------------------
    // Video polling infra (new)
    // Enabled only when: course.rabbitmq.video-polling.enabled=true
    // -----------------------------

    @Bean(name = "courseVideoPollingQueue")
    @ConditionalOnProperty(prefix = "course.rabbitmq.video-polling", name = "enabled", havingValue = "true")
    public Queue courseVideoPollingQueue() {
        return new Queue(pollingProps.getPollingQueue(), true);
    }

    /**
     * Delay queue with DLX:
     * - messages are published here with a PER-MESSAGE TTL (expiration)
     * - once expired, Rabbit dead-letters them back to the main exchange using pollingRoutingKey
     */
    @Bean(name = "courseVideoPollingDelayQueue")
    @ConditionalOnProperty(prefix = "course.rabbitmq.video-polling", name = "enabled", havingValue = "true")
    public Queue courseVideoPollingDelayQueue(@Qualifier("courseExchange") TopicExchange courseExchange) {

        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", courseExchange.getName());
        args.put("x-dead-letter-routing-key", pollingProps.getPollingRoutingKey());

        return new Queue(pollingProps.getPollingDelayQueue(), true, false, false, args);
    }

    @Bean
    @ConditionalOnProperty(prefix = "course.rabbitmq.video-polling", name = "enabled", havingValue = "true")
    public Binding courseVideoPollingBinding(
            @Qualifier("courseVideoPollingQueue") Queue pollingQueue,
            @Qualifier("courseExchange") TopicExchange courseExchange
    ) {
        return BindingBuilder.bind(pollingQueue)
                .to(courseExchange)
                .with(pollingProps.getPollingRoutingKey());
    }

    @Bean
    @ConditionalOnProperty(prefix = "course.rabbitmq.video-polling", name = "enabled", havingValue = "true")
    public Binding courseVideoPollingDelayBinding(
            @Qualifier("courseVideoPollingDelayQueue") Queue delayQueue,
            @Qualifier("courseExchange") TopicExchange courseExchange
    ) {
        return BindingBuilder.bind(delayQueue)
                .to(courseExchange)
                .with(pollingProps.getPollingDelayRoutingKey());
    }
}
