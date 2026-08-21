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
 * Detects prototype pollution patterns in JavaScript.
 * Patterns: obj[key] = value where key can be "__proto__", "constructor", "prototype".
 * Also detects direct __proto__ assignment.
 */
@Component
public class JSPrototypePollutionRule implements LineBasedRule {

    private static final Pattern PROTO_ASSIGN = Pattern.compile(
            "__proto__\\s*[\\[=]|constructor\\s*\\[|prototype\\s*\\["
    );

    private static final Pattern MERGE_PATTERN = Pattern.compile(
            "\\bObject\\.assign\\s*\\(.*,.*\\)|\\.\\.\\."
    );

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JS-SEC-006",
                "Prototype Pollution Risk",
                "Unvalidated property assignment via __proto__/constructor/prototype enables prototype pollution.",
                IssueType.SECURITY,
                IssueSeverity.HIGH
        );
    }

    @Override
    public List<CodeIssue> check(String fileName, List<String> lines) {
        List<CodeIssue> issues = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripLineComment(lines.get(i));
            if (PROTO_ASSIGN.matcher(line).find()) {
                issues.add(new CodeIssue(
                        fileName, i + 1,
                        IssueType.SECURITY, IssueSeverity.HIGH,
                        "JS-SEC-006",
                        "Potential prototype pollution — __proto__, constructor, or prototype assignment detected.",
                        "Validate keys before dynamic assignment. Use Object.create(null) for dictionaries. Use Map instead of plain objects."
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
