package com.codeguardian.analyzerservice.analyzer;

import com.codeguardian.analyzerservice.model.CodeIssue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic rule engine for line-based languages (Python, JavaScript).
 * Isolates rule failures — a single broken rule does not fail the file.
 */
@Service
public class LineBasedRuleEngine {

    public List<CodeIssue> analyze(
            String fileName,
            List<String> lines,
            List<? extends LineBasedRule> rules
    ) {
        List<CodeIssue> issues = new ArrayList<>();

        for (LineBasedRule rule : rules) {
            try {
                System.out.println(
                        "Running rule: "
                                + rule.metadata().id()
                                + " on "
                                + fileName
                );
                issues.addAll(rule.check(fileName, lines));
            } catch (Exception e) {
                // Rule failure must not crash the engine
                System.err.println(
                        "Rule "
                                + rule.metadata().id()
                                + " failed on "
                                + fileName
                                + ": "
                                + e.getMessage()
                );
            }
        }

        return issues;
    }
}
