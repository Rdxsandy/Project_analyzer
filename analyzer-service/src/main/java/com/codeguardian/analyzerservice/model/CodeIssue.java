package com.codeguardian.analyzerservice.model;

public class CodeIssue {

    private String file;
    private int line;
    private IssueType type;
    private IssueSeverity severity;
    private String rule;
    private String message;
    private String recommendation;
    private String sourceContext;

    public CodeIssue() {
    }

    public CodeIssue(
            String file,
            int line,
            IssueType type,
            IssueSeverity severity,
            String rule,
            String message,
            String recommendation
    ) {
        this.file = file;
        this.line = line;
        this.type = type;
        this.severity = severity;
        this.rule = rule;
        this.message = message;
        this.recommendation = recommendation;
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
}
