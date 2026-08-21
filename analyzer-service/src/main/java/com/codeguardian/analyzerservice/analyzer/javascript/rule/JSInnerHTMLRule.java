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
 * Detects .innerHTML assignment — XSS vulnerability.
 * Direct innerHTML assignment with unescaped data injects arbitrary HTML/JS.
 */
@Component
public class JSInnerHTMLRule implements LineBasedRule {

    private static final Pattern INNER_HTML = Pattern.compile(
            "\\.innerHTML\\s*="
    );

    private static final Pattern OUTER_HTML = Pattern.compile(
            "\\.outerHTML\\s*="
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JS-SEC-003",
                "innerHTML/outerHTML XSS",
                "Direct innerHTML/outerHTML assignment with unescaped data enables XSS.",
                IssueType.SECURITY,
                IssueSeverity.HIGH
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripLineComment(lines.get(i));
            if (INNER_HTML.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.HIGH,
                        "JS-SEC-003",
                        "innerHTML assignment detected — potential XSS vulnerability.",
                        "Use textContent for text, or sanitize HTML with DOMPurify before assigning to innerHTML."
                ));
            } else if (OUTER_HTML.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.HIGH,
                        "JS-SEC-003",
                        "outerHTML assignment detected — potential XSS vulnerability.",
                        "Use DOM manipulation methods or sanitize with DOMPurify."
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
