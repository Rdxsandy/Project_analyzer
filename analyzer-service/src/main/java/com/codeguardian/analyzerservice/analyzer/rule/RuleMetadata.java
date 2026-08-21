package com.codeguardian.analyzerservice.analyzer.rule;

import com.codeguardian.analyzerservice.model.IssueSeverity;
import com.codeguardian.analyzerservice.model.IssueType;

public record RuleMetadata(
        String id,
        String name,
        String description,
        IssueType type,
        IssueSeverity severity
) {
}
