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
 * Detects console.log() calls — debug output left in production code.
 * Severity is LOW: it's a quality issue, not directly a security risk.
 */
@Component
public class JSConsoleLogRule implements LineBasedRule {

    private static final Pattern CONSOLE = Pattern.compile(
            "\\bconsole\\.(log|debug|info|warn|error|trace|dir)\\s*\\("
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JS-QA-001",
                "Console Logging in Production",
                "console.log/debug/info calls left in production can leak sensitive data.",
                IssueType.CODE_QUALITY,
                IssueSeverity.LOW
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripLineComment(lines.get(i));
            if (CONSOLE.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.CODE_QUALITY, IssueSeverity.LOW,
                        "JS-QA-001",
                        "console logging detected — remove debug output before production deployment.",
                        "Remove console statements or replace with a structured logging library (e.g. Winston, Pino)."
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
