package com.codeguardian.aireviewservice.config;

import com.codeguardian.aireviewservice.messaging.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue aiReviewQueue() {
        return new Queue(
                RabbitMQConstants.AI_REVIEW_QUEUE,
                true
        );
    }
}
