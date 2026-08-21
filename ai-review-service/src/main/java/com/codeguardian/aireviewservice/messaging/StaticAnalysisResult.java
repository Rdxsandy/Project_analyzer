package com.codeguardian.aireviewservice.messaging;

import java.util.List;

public class StaticAnalysisResult {

    private Long scanId;
    private Long projectId;

    private List<StaticIssue> issues;

    public StaticAnalysisResult() {
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

    public List<StaticIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<StaticIssue> issues) {
        this.issues = issues;
    }
}
