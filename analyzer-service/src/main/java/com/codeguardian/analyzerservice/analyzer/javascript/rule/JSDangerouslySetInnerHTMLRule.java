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
 * Detects dangerouslySetInnerHTML in React — XSS risk.
 * This prop bypasses React's automatic escaping.
 */
@Component
public class JSDangerouslySetInnerHTMLRule implements LineBasedRule {

    private static final Pattern DANGEROUS = Pattern.compile(
            "dangerouslySetInnerHTML\\s*=\\s*\\{"
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JS-SEC-005",
                "dangerouslySetInnerHTML (React XSS)",
                "dangerouslySetInnerHTML bypasses React's XSS protection — can inject arbitrary HTML.",
                IssueType.SECURITY,
                IssueSeverity.HIGH
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        // Only relevant in JSX/TSX files
        String lower = fileName.toLowerCase();
        if (!lower.endsWith(".jsx") && !lower.endsWith(".tsx") && !lower.endsWith(".js") && !lower.endsWith(".ts")) {
            return issues;
        }
        for (int i = 0; i < lines.size(); i++) {
            if (DANGEROUS.matcher(lines.get(i)).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.HIGH,
                        "JS-SEC-005",
                        "dangerouslySetInnerHTML detected — XSS risk in React component.",
                        "Sanitize HTML with DOMPurify before using dangerouslySetInnerHTML, or render as text."
                ));
            }
        }
        return issues;
    }
}
