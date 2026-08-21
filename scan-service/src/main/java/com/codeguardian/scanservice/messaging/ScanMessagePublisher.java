package com.codeguardian.scanservice.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScanMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public ScanMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(ScanMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.SCAN_QUEUE,
                message
        );

        System.out.println(
                "Scan message published: scanId=" + message.getScanId()
                        + ", projectId=" + message.getProjectId()
        );
    }
}
