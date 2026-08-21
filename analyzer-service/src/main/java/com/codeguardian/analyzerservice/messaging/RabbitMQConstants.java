package com.codeguardian.analyzerservice.messaging;

public class RabbitMQConstants {

    public static final String SCAN_QUEUE =
            "codeguardian.scan.queue";

    public static final String RESULT_QUEUE =
            "codeguardian.scan.result.queue";

    private RabbitMQConstants() {
    }
}
