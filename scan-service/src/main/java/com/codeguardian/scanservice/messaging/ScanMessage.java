package com.codeguardian.scanservice.messaging;

public class ScanMessage {

    private Long scanId;
    private Long projectId;
    private String owner;
    private String repository;
    private String branch;
    private Long pullRequestNumber;
    private boolean incremental;

    public ScanMessage() {
    }

    public ScanMessage(
            Long scanId,
            Long projectId,
            String owner,
            String repository,
            String branch,
            Long pullRequestNumber,
            boolean incremental
    ) {
        this.scanId = scanId;
        this.projectId = projectId;
        this.owner = owner;
        this.repository = repository;
        this.branch = branch;
        this.pullRequestNumber = pullRequestNumber;
        this.incremental = incremental;
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Long getPullRequestNumber() {
        return pullRequestNumber;
    }

    public void setPullRequestNumber(Long pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }
}
