package com.codeguardian.scanservice.messaging;

public record ScanMessage(
        Long scanId,
        Long projectId,
        String repositoryOwner,
        String repositoryName,
        Integer pullRequestNumber,
        String commitSha
) {
}
