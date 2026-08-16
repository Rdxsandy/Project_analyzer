package com.codeguardian.projectservice.github.dto;

public record GitHubPullRequestResponse(
        Long id,
        Integer number,
        String title,
        String state,
        String html_url
) {
}
