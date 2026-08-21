package com.codeguardian.aireviewservice.model;

import java.util.List;

public class AIReviewResult {

    private Long scanId;
    private Long projectId;

    private List<AIReviewedIssue> issues;

    public AIReviewResult() {
    }

    public Long getScanId() {
        return scanId;
    }

    public void setScanId(Long scanId) {
        this.scanId = scanId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<AIReviewedIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<AIReviewedIssue> issues) {
        this.issues = issues;
    }
}
