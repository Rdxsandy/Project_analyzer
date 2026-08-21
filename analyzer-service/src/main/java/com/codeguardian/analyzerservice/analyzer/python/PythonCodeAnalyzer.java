package com.codeguardian.analyzerservice.analyzer.python;

import com.codeguardian.analyzerservice.analyzer.LineBasedRule;
import com.codeguardian.analyzerservice.analyzer.LineBasedRuleEngine;
import com.codeguardian.analyzerservice.analyzer.FileScanner;
import com.codeguardian.analyzerservice.analyzer.SourceContextExtractor;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.analyzer.python.rule.*;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates full Python repository analysis.
 * Uses parallel stream for large repositories.
 */
@Service
public class PythonCodeAnalyzer {

    private final FileScanner fileScanner;
    private final LineBasedRuleEngine ruleEngine;
    private final SourceContextExtractor contextExtractor;
    private final List<LineBasedRule> pythonRules;

    public PythonCodeAnalyzer(
            FileScanner fileScanner,
            LineBasedRuleEngine ruleEngine,
            SourceContextExtractor contextExtractor,
            PythonHardcodedSecretRule hardcodedSecretRule,
            PythonEvalRule evalRule,
            PythonPickleRule pickleRule,
            PythonSQLInjectionRule sqlInjectionRule,
            PythonSubprocessShellRule subprocessShellRule,
            PythonDebugModeRule debugModeRule,
            PythonAssertSecurityRule assertSecurityRule
    ) {
        this.fileScanner = fileScanner;
        this.ruleEngine = ruleEngine;
        this.contextExtractor = contextExtractor;
        this.pythonRules = List.of(
                hardcodedSecretRule,
                evalRule,
                pickleRule,
                sqlInjectionRule,
                subprocessShellRule,
                debugModeRule,
                assertSecurityRule
        );
    }

    public List<CodeIssue> analyzeWorkspace(Path workspace) throws Exception {
        List<Path> files = fileScanner.findFiles(workspace.toString(), ".py");

        // Parallel analysis for large repos, then collect
        return files.parallelStream()
                .flatMap(file -> {
                    try {
                        List<String> lines = fileScanner.readLines(file);
                        String relPath = workspace.relativize(file).toString();
                        List<CodeIssue> issues = ruleEngine.analyze(relPath, lines, pythonRules);
                        issues.forEach(issue ->
                                issue.setSourceContext(contextExtractor.extract(lines, issue.getLine()))
                        );
                        return issues.stream();
                    } catch (Exception e) {
                        System.err.println("Error analyzing Python file " + file + ": " + e.getMessage());
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public List<CodeIssue> analyzeFiles(List<Path> files, Path workspace) {
        List<CodeIssue> issues = new ArrayList<>();
        for (Path file : files) {
            try {
                List<String> lines = fileScanner.readLines(file);
                String relPath = workspace.relativize(file).toString();
                List<CodeIssue> fileIssues = ruleEngine.analyze(relPath, lines, pythonRules);
                fileIssues.forEach(issue ->
                        issue.setSourceContext(contextExtractor.extract(lines, issue.getLine()))
                );
                issues.addAll(fileIssues);
            } catch (Exception e) {
                System.err.println("Error analyzing Python file " + file + ": " + e.getMessage());
            }
        }
        return issues;
    }
}
