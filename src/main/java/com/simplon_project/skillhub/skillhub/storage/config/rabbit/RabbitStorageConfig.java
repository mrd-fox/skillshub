package com.simplon_project.skillhub.skillhub.storage.config.rabbit;

import com.simplon_project.skillhub.skillhub.common.messaging.RabbitCommonConnectionProps;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class RabbitStorageConfig {

    private final RabbitCommonConnectionProps connectionProps;
    private final RabbitStorageProps rabbitStorageProps;

    @Bean(name = "storageRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory storageRabbitListenerContainerFactory(
            @Qualifier("storageConnectionFactory") CachingConnectionFactory connectionFactory,
            @Qualifier("storageMessageConverter") Jackson2JsonMessageConverter messageConverter) {

        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean(name = "storageConnectionFactory")
    public CachingConnectionFactory connectionFactory() {
        var factory = new CachingConnectionFactory(connectionProps.getHost(), connectionProps.getPort());
        factory.setUsername(connectionProps.getUsername());
        factory.setPassword(connectionProps.getPassword());
        factory.setVirtualHost(connectionProps.getVirtualHost());
        factory.setConnectionTimeout(connectionProps.getConnectionTimeout());
        return factory;
    }

    @Bean(name = "storageExchange")
    public TopicExchange storageExchange() {
        return new TopicExchange(rabbitStorageProps.getExchange(), true, false);
    }

    @Bean(name = "storageUploadedQueue")
    public Queue storageUploadedQueue() {
        return new Queue(rabbitStorageProps.getUploadedQueue(), true);
    }

    @Bean(name = "storageMetadataQueue")
    public Queue storageMetadataQueue() {
        return new Queue(rabbitStorageProps.getMetadataQueue(), true);
    }

    @Bean
    public Binding updloadedBinding(
            @Qualifier("storageUploadedQueue") Queue videoUploadedQueue,
            @Qualifier("storageExchange") TopicExchange courseExchange) {
        return BindingBuilder.bind(videoUploadedQueue)
                .to(courseExchange)
                .with(rabbitStorageProps.getUploadRoutingKey());
    }

    @Bean
    public Binding metadataBinding(
            @Qualifier("storageMetadataQueue") Queue videoUploadedQueue,
            @Qualifier("storageExchange") TopicExchange courseExchange) {
        return BindingBuilder.bind(videoUploadedQueue)
                .to(courseExchange)
                .with(rabbitStorageProps.getMetadataRoutingKey());
    }

    @Bean(name = "storageMessageConverter")
    public Jackson2JsonMessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = "storageRabbitTemplate")
    public RabbitTemplate rabbitTemplate(
            @Qualifier("storageConnectionFactory") CachingConnectionFactory connectionFactory,
            @Qualifier("storageMessageConverter") Jackson2JsonMessageConverter messageConverter
    ) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(rabbitStorageProps.getExchange());
        return template;
    }
}
