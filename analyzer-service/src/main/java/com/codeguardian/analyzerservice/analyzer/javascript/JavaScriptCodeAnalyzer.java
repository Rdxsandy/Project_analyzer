package com.codeguardian.analyzerservice.analyzer.javascript;

import com.codeguardian.analyzerservice.analyzer.LineBasedRule;
import com.codeguardian.analyzerservice.analyzer.LineBasedRuleEngine;
import com.codeguardian.analyzerservice.analyzer.FileScanner;
import com.codeguardian.analyzerservice.analyzer.SourceContextExtractor;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.analyzer.javascript.rule.*;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates full JavaScript/TypeScript repository analysis.
 * Covers .js, .ts, .jsx, .tsx files.
 * Uses parallel stream for large repositories.
 */
@Service
public class JavaScriptCodeAnalyzer {

    private final FileScanner fileScanner;
    private final LineBasedRuleEngine ruleEngine;
    private final SourceContextExtractor contextExtractor;
    private final List<LineBasedRule> jsRules;

    public JavaScriptCodeAnalyzer(
            FileScanner fileScanner,
            LineBasedRuleEngine ruleEngine,
            SourceContextExtractor contextExtractor,
            JSHardcodedSecretRule hardcodedSecretRule,
            JSEvalRule evalRule,
            JSInnerHTMLRule innerHTMLRule,
            JSDocumentWriteRule documentWriteRule,
            JSDangerouslySetInnerHTMLRule dangerouslySetInnerHTMLRule,
            JSConsoleLogRule consoleLogRule,
            JSPrototypePollutionRule prototypePollutionRule
    ) {
        this.fileScanner = fileScanner;
        this.ruleEngine = ruleEngine;
        this.contextExtractor = contextExtractor;
        this.jsRules = List.of(
                hardcodedSecretRule,
                evalRule,
                innerHTMLRule,
                documentWriteRule,
                dangerouslySetInnerHTMLRule,
                consoleLogRule,
                prototypePollutionRule
        );
    }

    public List<CodeIssue> analyzeWorkspace(Path workspace) throws Exception {
        List<Path> files = fileScanner.findFiles(
                workspace.toString(), ".js", ".ts", ".jsx", ".tsx"
        );

        return files.parallelStream()
                .flatMap(file -> {
                    try {
                        List<String> lines = fileScanner.readLines(file);
                        String relPath = workspace.relativize(file).toString();
                        List<CodeIssue> issues = ruleEngine.analyze(relPath, lines, jsRules);
                        issues.forEach(issue ->
                                issue.setSourceContext(contextExtractor.extract(lines, issue.getLine()))
                        );
                        return issues.stream();
                    } catch (Exception e) {
                        System.err.println("Error analyzing JS file " + file + ": " + e.getMessage());
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    public List<CodeIssue> analyzeFiles(List<Path> files, Path workspace) {
        List<CodeIssue> issues = new ArrayList<>();
        for (Path file : files) {
            try {
                List<String> lines = fileScanner.readLines(file);
                String relPath = workspace.relativize(file).toString();
                List<CodeIssue> fileIssues = ruleEngine.analyze(relPath, lines, jsRules);
                fileIssues.forEach(issue ->
                        issue.setSourceContext(contextExtractor.extract(lines, issue.getLine()))
                );
                issues.addAll(fileIssues);
            } catch (Exception e) {
                System.err.println("Error analyzing JS file " + file + ": " + e.getMessage());
            }
        }
        return issues;
    }
}
