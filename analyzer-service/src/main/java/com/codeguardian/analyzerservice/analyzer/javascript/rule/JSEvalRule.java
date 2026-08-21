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
 * Detects eval() usage in JavaScript/TypeScript — code injection risk.
 * Also covers new Function(...) which is functionally equivalent.
 */
@Component
public class JSEvalRule implements LineBasedRule {

    private static final Pattern EVAL_PATTERN = Pattern.compile(
            "\\beval\\s*\\("
    );

    private static final Pattern NEW_FUNCTION = Pattern.compile(
            "\\bnew\\s+Function\\s*\\("
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JS-SEC-002",
                "Use of eval() or new Function()",
                "eval() and new Function() execute arbitrary strings as code — severe injection risk.",
                IssueType.SECURITY,
                IssueSeverity.CRITICAL
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripLineComment(lines.get(i));
            if (EVAL_PATTERN.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.CRITICAL,
                        "JS-SEC-002",
                        "eval() detected — arbitrary code execution risk.",
                        "Remove eval(). Parse JSON with JSON.parse(). Use explicit logic instead of dynamic execution."
                ));
            } else if (NEW_FUNCTION.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.CRITICAL,
                        "JS-SEC-002",
                        "new Function() detected — equivalent to eval(), arbitrary code execution risk.",
                        "Replace new Function() with explicit function definitions."
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
