package com.codeguardian.analyzerservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codeguardian.analyzerservice.messaging.RabbitMQConstants;

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
    public org.springframework.amqp.core.TopicExchange analysisExchange() {
        return new org.springframework.amqp.core.TopicExchange(
                "codeguardian.analysis.exchange"
        );
    }

    @Bean
    public Queue scanResultQueue() {
        return new Queue(
                "codeguardian.scan.result.queue",
                true
        );
    }

    @Bean
    public Queue githubResultQueue() {
        return new Queue(
                "codeguardian.github.result.queue",
                true
        );
    }

    @Bean
    public org.springframework.amqp.core.Binding scanResultBinding(
            Queue scanResultQueue,
            org.springframework.amqp.core.TopicExchange analysisExchange
    ) {
        return org.springframework.amqp.core.BindingBuilder
                .bind(scanResultQueue)
                .to(analysisExchange)
                .with("analysis.result");
    }

    @Bean
    public org.springframework.amqp.core.Binding githubResultBinding(
            Queue githubResultQueue,
            org.springframework.amqp.core.TopicExchange analysisExchange
    ) {
        return org.springframework.amqp.core.BindingBuilder
                .bind(githubResultQueue)
                .to(analysisExchange)
                .with("analysis.result");
    }
    @Bean
    public Queue aiReviewQueue() {
        return new Queue(
                "codeguardian.ai.review.queue",
                true
        );
    }

    @Bean
    public org.springframework.amqp.core.Binding aiReviewBinding(
            Queue aiReviewQueue,
            org.springframework.amqp.core.TopicExchange analysisExchange
    ) {
        return org.springframework.amqp.core.BindingBuilder
                .bind(aiReviewQueue)
                .to(analysisExchange)
                .with("analysis.result");
    }
}
