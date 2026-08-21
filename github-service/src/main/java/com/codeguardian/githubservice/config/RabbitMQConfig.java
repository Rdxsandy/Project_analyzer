package com.codeguardian.githubservice.config;

import com.codeguardian.githubservice.messaging.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue resultQueue() {
        return new Queue(
                RabbitMQConstants.RESULT_QUEUE,
                true
        );
    }
}
