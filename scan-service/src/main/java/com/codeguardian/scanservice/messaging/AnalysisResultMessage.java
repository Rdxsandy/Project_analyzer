package com.codeguardian.scanservice.messaging;

import java.util.List;

public class AnalysisResultMessage {

    private Long scanId;
    private Long projectId;
    private String owner;
    private String repository;
    private Long pullRequestNumber;
    private int totalIssues;
    private List<AnalysisIssueMessage> issues;

    public AnalysisResultMessage() {
    }

    public Long getScanId() {
        return scanId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepository() {
        return repository;
    }

    public Long getPullRequestNumber() {
        return pullRequestNumber;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setPullRequestNumber(Long pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }

    public int getTotalIssues() {
        return totalIssues;
    }

    public List<AnalysisIssueMessage> getIssues() {
        return issues;
    }

    public void setScanId(Long scanId) {
        this.scanId = scanId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setTotalIssues(int totalIssues) {
        this.totalIssues = totalIssues;
    }

    public void setIssues(
            List<AnalysisIssueMessage> issues
    ) {
        this.issues = issues;
    }
}
