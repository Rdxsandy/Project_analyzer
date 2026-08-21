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
 * Detects DEBUG = True in Python configuration files.
 * Django and Flask expose stack traces and internal info when DEBUG is on.
 */
@Component
public class PythonDebugModeRule implements LineBasedRule {

    private static final Pattern DEBUG_TRUE = Pattern.compile(
            "^\\s*DEBUG\\s*=\\s*True\\s*$"
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "PY-CFG-001",
                "Debug Mode Enabled",
                "DEBUG = True exposes stack traces, internal data and disables security checks.",
                IssueType.SECURITY,
                IssueSeverity.HIGH
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        // Only flag in likely settings/config files
        String lowerName = fileName.toLowerCase();
        boolean isConfig = lowerName.contains("settings")
                || lowerName.contains("config")
                || lowerName.endsWith("settings.py");

        if (!isConfig) return issues;

        for (int i = 0; i < lines.size(); i++) {
            if (DEBUG_TRUE.matcher(lines.get(i)).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.HIGH,
                        "PY-CFG-001",
                        "DEBUG = True detected in configuration — must not be deployed to production.",
                        "Use environment variables: DEBUG = os.getenv('DEBUG', 'False') == 'True'"
                ));
            }
        }
        return issues;
    }
}
