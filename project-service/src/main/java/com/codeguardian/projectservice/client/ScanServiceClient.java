package com.codeguardian.projectservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ScanServiceClient {

    private final RestClient restClient;

    public ScanServiceClient(@Value("${scan-service.url:http://localhost:8082}") String scanServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(scanServiceUrl)
                .build();
    }

    public void createScan(ScanRequest request) {
        restClient.post()
                .uri("/api/scans")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
