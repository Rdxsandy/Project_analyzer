package com.codeguardian.scanservice.client;

import com.codeguardian.scanservice.exception.ProjectNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ProjectServiceClient {

    private final RestClient restClient;

    public ProjectServiceClient(@Value("${project-service.url:http://localhost:8081}") String projectServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(projectServiceUrl)
                .build();
    }

    public void verifyProjectExists(Long projectId) {
        try {
            restClient.get()
                    .uri("/api/projects/{id}", projectId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (org.springframework.web.client.RestClientResponseException e) {
            throw new ProjectNotFoundException("Project with ID " + projectId + " does not exist or could not be validated.");
        }
    }
}
