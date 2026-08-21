package com.codeguardian.analyzerservice.model;

import java.util.List;

public class AnalysisResult {

    private Long scanId;
    private Long projectId;
    private String owner;
    private String repository;
    private Long pullRequestNumber;
    private int totalIssues;
    private List<CodeIssue> issues;

    public AnalysisResult() {
    }

    public AnalysisResult(
            Long scanId,
            Long projectId,
            String owner,
            String repository,
            Long pullRequestNumber,
            List<CodeIssue> issues
    ) {
        this.scanId = scanId;
        this.projectId = projectId;
        this.owner = owner;
        this.repository = repository;
        this.pullRequestNumber = pullRequestNumber;
        this.issues = issues;
        this.totalIssues = issues.size();
    }

    public Long getScanId() {
        return scanId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public int getTotalIssues() {
        return totalIssues;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public Long getPullRequestNumber() {
        return pullRequestNumber;
    }

    public void setPullRequestNumber(Long pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }

    public List<CodeIssue> getIssues() {
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

    public void setIssues(List<CodeIssue> issues) {
        this.issues = issues;
    }
}
