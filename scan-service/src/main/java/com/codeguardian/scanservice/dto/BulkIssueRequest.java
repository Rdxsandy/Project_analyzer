package com.codeguardian.scanservice.dto;

/**
 * A single issue sent from upload-service via POST /api/scans/{id}/issues/bulk
 */
public class BulkIssueRequest {

    private String severity;
    private String type;
    private String message;
    private String filePath;
    private Integer lineNumber;
    private String ruleId;
    private String suggestion;

    public BulkIssueRequest() {}

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
}
