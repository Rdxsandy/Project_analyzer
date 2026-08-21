package com.codeguardian.analyzerservice.analyzer;

import com.codeguardian.analyzerservice.model.AnalysisResult;

public interface CodeAnalyzer {

    AnalysisResult analyze(
            Long scanId,
            Long projectId,
            String owner,
            String repository,
            String branch,
            Long pullRequestNumber,
            boolean incremental
    );
}
