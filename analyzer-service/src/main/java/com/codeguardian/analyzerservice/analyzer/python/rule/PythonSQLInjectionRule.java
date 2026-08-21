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
 * Detects SQL injection patterns in Python:
 *  - String formatting inside execute(): cursor.execute("SELECT ... %s" % val)
 *  - f-string interpolation in SQL: f"SELECT ... {user_input}"
 *  - String concatenation: "SELECT " + variable
 */
@Component
public class PythonSQLInjectionRule implements LineBasedRule {

    // % formatting in execute
    private static final Pattern FORMAT_EXECUTE = Pattern.compile(
            "\\.execute\\s*\\(\\s*[\"'].*[\"']\\s*%"
    );

    // f-string with SQL keyword
    private static final Pattern FSTRING_SQL = Pattern.compile(
            "\\.execute\\s*\\(\\s*f[\"'].*\\{.*\\}"
    );

    // String concat with SQL keyword
    private static final Pattern CONCAT_SQL = Pattern.compile(
            "\\.execute\\s*\\(\\s*[\"']\\s*(SELECT|INSERT|UPDATE|DELETE|DROP|ALTER)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "PY-SEC-004",
                "SQL Injection Risk",
                "User-controlled values interpolated directly into SQL queries.",
                IssueType.SECURITY,
                IssueSeverity.CRITICAL
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripComment(lines.get(i));
            if (FORMAT_EXECUTE.matcher(line).find()
                    || FSTRING_SQL.matcher(line).find()
                    || CONCAT_SQL.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.CRITICAL,
                        "PY-SEC-004",
                        "Possible SQL injection — user input interpolated into query.",
                        "Use parameterized queries: cursor.execute(query, (param,)) instead of string formatting."
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
