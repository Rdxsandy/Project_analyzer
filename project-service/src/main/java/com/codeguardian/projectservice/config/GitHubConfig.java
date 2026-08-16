package com.codeguardian.projectservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GitHubConfig {

    @Bean
    public RestClient githubRestClient(
            @Value("${github.api-url}") String apiUrl,
            @Value("${github.token}") String token) {

        return RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }
}
