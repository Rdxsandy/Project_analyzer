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
 * Detects hardcoded secrets in Python: passwords, tokens, API keys, secrets.
 * Pattern: variable_name = "some_value"
 * Pre-compiled at class load — zero overhead per file.
 */
@Component
public class PythonHardcodedSecretRule implements LineBasedRule {

    // Matches: secret_name = "value" or secret_name = 'value'
    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "(?i)\\b(password|passwd|secret|api[_-]?key|access[_-]?key|" +
            "auth[_-]?token|private[_-]?key|client[_-]?secret|db[_-]?pass|" +
            "database[_-]?password|credentials)\\s*=\\s*['\"][^'\"]{4,}['\"]",
            Pattern.CASE_INSENSITIVE
    );

    // Exclude common placeholder values
    private static final Pattern PLACEHOLDER = Pattern.compile(
            "(?i)(your[_-]|<|\\$\\{|%s|xxx|placeholder|example|changeme|dummy|test123|todo)"
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "PY-SEC-001",
                "Hardcoded Secret",
                "Detects hardcoded passwords, API keys, and secrets in Python source.",
                IssueType.SECURITY,
                IssueSeverity.CRITICAL
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        String stripped;
        for (int i = 0; i < lines.size(); i++) {
            stripped = stripComment(lines.get(i));
            Matcher m = SECRET_PATTERN.matcher(stripped);
            if (m.find() && !PLACEHOLDER.matcher(m.group()).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.CRITICAL,
                        "PY-SEC-001",
                        "Hardcoded secret detected: " + m.group(1),
                        "Store secrets in environment variables or a secrets manager (e.g. python-dotenv, AWS Secrets Manager)."
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
