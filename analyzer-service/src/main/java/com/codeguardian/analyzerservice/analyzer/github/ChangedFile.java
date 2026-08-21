package com.codeguardian.analyzerservice.analyzer.github;

public record ChangedFile(
        String path,
        String status
) {
}
