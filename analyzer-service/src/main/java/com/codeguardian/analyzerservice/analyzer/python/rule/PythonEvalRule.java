package com.codeguardian.analyzerservice.analyzer.python.rule;

import com.codeguardian.analyzerservice.analyzer.LineBasedRule;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.model.IssueSeverity;
import com.codeguardian.analyzerservice.model.IssueType;
import com.codeguardian.analyzerservice.analyzer.rule.RuleMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects use of eval() in Python — arbitrary code execution risk.
 */
@Component
public class PythonEvalRule implements LineBasedRule {

    private static final Pattern EVAL_PATTERN = Pattern.compile(
            "\\beval\\s*\\("
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "PY-SEC-002",
                "Use of eval()",
                "eval() executes arbitrary code from a string — severe injection risk.",
                IssueType.SECURITY,
                IssueSeverity.CRITICAL
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String stripped = stripComment(lines.get(i));
            Matcher m = EVAL_PATTERN.matcher(stripped);
            if (m.find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.CRITICAL,
                        "PY-SEC-002",
                        "Use of eval() detected — arbitrary code execution risk.",
                        "Replace eval() with safer alternatives: ast.literal_eval() for data, or explicit parsing logic."
                ));
            }
        }
        return issues;
    }

    private String stripComment(String line) {
        int idx = line.indexOf('#');
        return idx >= 0 ? line.substring(0, idx) : line;
    }
}
