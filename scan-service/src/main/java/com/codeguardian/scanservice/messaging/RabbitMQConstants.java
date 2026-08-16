package com.codeguardian.scanservice.messaging;

public final class RabbitMQConstants {

    private RabbitMQConstants() {
    }

    public static final String SCAN_EXCHANGE = "codeguardian.scan.exchange";

    public static final String SCAN_QUEUE = "codeguardian.scan.queue";

    public static final String SCAN_ROUTING_KEY = "scan.created";
}
