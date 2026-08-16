package com.codeguardian.scanservice.dto;

import com.codeguardian.scanservice.entity.IssueSeverity;
import com.codeguardian.scanservice.entity.IssueType;

public record ScanIssueResponse(
        Long id,
        IssueSeverity severity,
        IssueType type,
        String message,
        String filePath,
        Integer lineNumber,
        String ruleId,
        String suggestion
) {
}
