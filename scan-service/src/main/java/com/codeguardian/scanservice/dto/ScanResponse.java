package com.codeguardian.scanservice.dto;

import com.codeguardian.scanservice.entity.ScanStatus;

import java.time.LocalDateTime;

public record ScanResponse(
        Long id,
        Long projectId,
        String repositoryOwner,
        String repositoryName,
        Integer pullRequestNumber,
        ScanStatus status,
        String commitSha,
        Integer totalFiles,
        Integer totalIssues,
        Integer criticalIssues,
        Integer highIssues,
        Integer mediumIssues,
        Integer lowIssues,
        Integer qualityScore,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}
