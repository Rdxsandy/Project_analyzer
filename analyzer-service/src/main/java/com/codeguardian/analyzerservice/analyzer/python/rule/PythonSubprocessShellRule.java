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
 * Detects subprocess calls with shell=True — shell injection risk.
 * e.g. subprocess.run(user_input, shell=True)
 */
@Component
public class PythonSubprocessShellRule implements LineBasedRule {

    private static final Pattern SHELL_TRUE = Pattern.compile(
            "\\bsubprocess\\.(run|call|Popen|check_output|check_call)\\s*\\(.*shell\\s*=\\s*True"
    );

    private static final Pattern OS_SYSTEM = Pattern.compile(
            "\\bos\\.system\\s*\\("
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "PY-SEC-005",
                "Shell Injection Risk",
                "subprocess with shell=True or os.system() allows OS command injection.",
                IssueType.SECURITY,
                IssueSeverity.HIGH
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripComment(lines.get(i));
            if (SHELL_TRUE.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.HIGH,
                        "PY-SEC-005",
                        "subprocess called with shell=True — OS command injection risk.",
                        "Pass command as a list without shell=True: subprocess.run(['cmd', 'arg'])."
                ));
            } else if (OS_SYSTEM.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.HIGH,
                        "PY-SEC-005",
                        "os.system() detected — OS command injection risk.",
                        "Use subprocess.run() with a list of arguments instead of os.system()."
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
