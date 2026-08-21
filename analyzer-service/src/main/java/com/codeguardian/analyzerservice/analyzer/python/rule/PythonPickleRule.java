package com.codeguardian.analyzerservice.analyzer.python.rule;

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
 * Detects unsafe deserialization via pickle.loads() / pickle.load().
 * Deserializing untrusted pickle data leads to arbitrary code execution.
 */
@Component
public class PythonPickleRule implements LineBasedRule {

    private static final Pattern PICKLE_PATTERN = Pattern.compile(
            "\\bpickle\\.loads?\\s*\\("
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "PY-SEC-003",
                "Unsafe Deserialization (pickle)",
                "pickle.load/loads on untrusted data allows arbitrary code execution.",
                IssueType.SECURITY,
                IssueSeverity.CRITICAL
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (PICKLE_PATTERN.matcher(stripComment(lines.get(i))).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.CRITICAL,
                        "PY-SEC-003",
                        "Unsafe deserialization with pickle.load(s) detected.",
                        "Use JSON, MessagePack, or validate the source before deserializing. Never unpickle untrusted data."
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
