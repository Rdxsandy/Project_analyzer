package com.codeguardian.scanservice.messaging;

import com.codeguardian.scanservice.entity.IssueSeverity;
import com.codeguardian.scanservice.entity.IssueType;

public class AnalysisIssueMessage {

    private String file;
    private int line;
    private IssueType type;
    private IssueSeverity severity;
    private String rule;
    private String message;
    private String recommendation;
    private String sourceContext;

    public AnalysisIssueMessage() {
    }

    public String getSourceContext() {
        return sourceContext;
    }

    public void setSourceContext(String sourceContext) {
        this.sourceContext = sourceContext;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public IssueType getType() {
        return type;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public String getRule() {
        return rule;
    }

    public String getMessage() {
        return message;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public void setSeverity(IssueSeverity severity) {
        this.severity = severity;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
