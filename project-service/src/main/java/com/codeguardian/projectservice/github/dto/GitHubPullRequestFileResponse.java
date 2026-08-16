package com.codeguardian.projectservice.github.dto;

public record GitHubPullRequestFileResponse(
        String sha,
        String filename,
        String status,
        Integer additions,
        Integer deletions,
        Integer changes,
        String raw_url
) {
}
