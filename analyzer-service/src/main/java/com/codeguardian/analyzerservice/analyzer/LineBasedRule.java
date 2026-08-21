package com.codeguardian.analyzerservice.analyzer;

import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.analyzer.rule.RuleMetadata;

import java.util.List;

/**
 * Shared interface for all line-based rules (Python, JavaScript).
 * Implementations must use pre-compiled static Pattern constants.
 * One rule failure must never propagate to the caller — handle internally.
 */
public interface LineBasedRule {

    RuleMetadata metadata();

    /**
     * Analyze a list of source lines.
     * Lines are 1-indexed in CodeIssue.line (lines.get(0) == line 1).
     */
    List<CodeIssue> check(String fileName, List<String> lines);
}
