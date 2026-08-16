package com.codeguardian.projectservice.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepositoryResponse(
        Long id,
        String name,
        String full_name,
        String html_url,
        String default_branch,
        String language,
        @JsonProperty("private") boolean private_repository
) {
}
