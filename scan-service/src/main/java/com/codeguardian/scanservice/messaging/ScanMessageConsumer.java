package com.codeguardian.scanservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ScanMessageConsumer {

    @RabbitListener(queues = RabbitMQConstants.SCAN_QUEUE)
    public void consumeScan(ScanMessage message) {

        System.out.println(
                "Received scan message: " + message
        );
    }
}
