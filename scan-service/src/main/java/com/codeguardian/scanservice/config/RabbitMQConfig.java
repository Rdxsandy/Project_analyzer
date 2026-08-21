package com.codeguardian.scanservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codeguardian.scanservice.messaging.RabbitMQConstants;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue scanQueue() {
        return new Queue(
                RabbitMQConstants.SCAN_QUEUE,
                true
        );
    }

    @Bean
    public Queue resultQueue() {
        return new Queue(
                RabbitMQConstants.RESULT_QUEUE,
                true
        );
    }

    @Bean
    public Queue aiResultQueue() {
        return new Queue(
                RabbitMQConstants.AI_RESULT_QUEUE,
                true
        );
    }

    @Bean
    public org.springframework.amqp.core.TopicExchange aiExchange() {
        return new org.springframework.amqp.core.TopicExchange(
                RabbitMQConstants.AI_EXCHANGE
        );
    }

    @Bean
    public org.springframework.amqp.core.Binding aiResultBinding(
            Queue aiResultQueue,
            org.springframework.amqp.core.TopicExchange aiExchange
    ) {
        return org.springframework.amqp.core.BindingBuilder
                .bind(aiResultQueue)
                .to(aiExchange)
                .with("ai.result");
    }
}
