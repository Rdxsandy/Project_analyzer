package com.codeguardian.analyzerservice.analyzer.javascript.rule;

import com.codeguardian.analyzerservice.analyzer.LineBasedRule;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.model.IssueSeverity;
import com.codeguardian.analyzerservice.model.IssueType;
import com.codeguardian.analyzerservice.analyzer.rule.RuleMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Detects document.write() — XSS risk and blocks the parser.
 */
@Component
public class JSDocumentWriteRule implements LineBasedRule {

    private static final Pattern DOC_WRITE = Pattern.compile(
            "\\bdocument\\.write(?:ln)?\\s*\\("
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JS-SEC-004",
                "document.write() Usage",
                "document.write() with user data enables XSS and blocks HTML parser.",
                IssueType.SECURITY,
                IssueSeverity.HIGH
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (DOC_WRITE.matcher(stripLineComment(lines.get(i))).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.HIGH,
                        "JS-SEC-004",
                        "document.write() detected — potential XSS and parser-blocking.",
                        "Use DOM APIs (createElement, appendChild) or sanitized innerHTML with DOMPurify."
                ));
            }
        }
        return issues;
    }

    private String stripLineComment(String line) {
        int idx = line.indexOf("//");
        return idx >= 0 ? line.substring(0, idx) : line;
    }
}
