package com.codeguardian.scanservice.config;

import com.codeguardian.scanservice.messaging.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange scanExchange() {
        return new DirectExchange(
                RabbitMQConstants.SCAN_EXCHANGE
        );
    }

    @Bean
    public Queue scanQueue() {
        return new Queue(
                RabbitMQConstants.SCAN_QUEUE,
                true
        );
    }

    @Bean
    public Binding scanBinding(
            Queue scanQueue,
            DirectExchange scanExchange) {

        return BindingBuilder
                .bind(scanQueue)
                .to(scanExchange)
                .with(RabbitMQConstants.SCAN_ROUTING_KEY);
    }
}
