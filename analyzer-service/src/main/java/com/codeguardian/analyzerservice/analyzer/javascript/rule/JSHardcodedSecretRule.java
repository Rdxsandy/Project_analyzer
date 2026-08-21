package com.codeguardian.analyzerservice.analyzer.javascript.rule;

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
 * Detects hardcoded secrets in JavaScript/TypeScript.
 * Covers const/let/var assignments, object literals, and environment variable names.
 */
@Component
public class JSHardcodedSecretRule implements LineBasedRule {

    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "(?i)(?:const|let|var|['\"])?\\s*(?:password|passwd|secret|apiKey|api_key|" +
            "accessKey|access_key|authToken|auth_token|privateKey|private_key|" +
            "clientSecret|client_secret|dbPassword|database_password)\\s*[:=]\\s*" +
            "['\"`][^'\"`]{4,}['\"`]"
    );

    private static final Pattern PLACEHOLDER = Pattern.compile(
            "(?i)(your[_-]|<|\\$\\{|process\\.env|placeholder|example|changeme|xxx|todo|test123)"
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JS-SEC-001",
                "Hardcoded Secret",
                "Detects hardcoded passwords, API keys, and secrets in JavaScript/TypeScript.",
                IssueType.SECURITY,
                IssueSeverity.CRITICAL
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripLineComment(lines.get(i));
            Matcher m = SECRET_PATTERN.matcher(line);
            if (m.find() && !PLACEHOLDER.matcher(m.group()).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.CRITICAL,
                        "JS-SEC-001",
                        "Hardcoded secret detected in JavaScript/TypeScript.",
                        "Use process.env.SECRET_NAME or a secrets manager. Never commit credentials."
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
