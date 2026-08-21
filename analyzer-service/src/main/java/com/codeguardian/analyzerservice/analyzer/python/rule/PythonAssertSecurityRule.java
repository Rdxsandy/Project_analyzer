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
 * Detects assert statements used for access control or input validation.
 * assert statements are stripped in optimized mode (python -O).
 */
@Component
public class PythonAssertSecurityRule implements LineBasedRule {

    // assert used with auth/permission/valid/check patterns
    private static final Pattern ASSERT_SECURITY = Pattern.compile(
            "^\\s*assert\\s+.*(is_authenticated|has_permission|is_valid|check|authorize|access|allowed)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "PY-SEC-006",
                "Assert for Security Check",
                "assert statements are removed with python -O; never use assert for security enforcement.",
                IssueType.SECURITY,
                IssueSeverity.MEDIUM
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (ASSERT_SECURITY.matcher(lines.get(i)).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.MEDIUM,
                        "PY-SEC-006",
                        "Security check using assert — will be disabled in optimized mode.",
                        "Replace assert with an explicit if + raise (e.g. PermissionDenied or ValueError)."
                ));
            }
        }
        return issues;
    }
}
