package com.codeguardian.analyzerservice.analyzer;

import com.codeguardian.analyzerservice.analyzer.github.GitHubRepositoryService;
import com.codeguardian.analyzerservice.analyzer.github.RepositoryWorkspaceService;
import com.codeguardian.analyzerservice.analyzer.javascript.IncrementalJavaScriptAnalyzer;
import com.codeguardian.analyzerservice.analyzer.javascript.JavaScriptCodeAnalyzer;
import com.codeguardian.analyzerservice.analyzer.python.IncrementalPythonAnalyzer;
import com.codeguardian.analyzerservice.analyzer.python.PythonCodeAnalyzer;
import com.codeguardian.analyzerservice.model.AnalysisResult;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.model.Language;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Routes analysis to the correct language-specific analyzer.
 *
 * Decision flow:
 *   ScanMessage.language
 *       ├── JAVA        → JavaCodeAnalyzer (full) / IncrementalJavaAnalyzer (PR)
 *       ├── PYTHON      → PythonCodeAnalyzer (full) / IncrementalPythonAnalyzer (PR)
 *       └── JAVASCRIPT  → JavaScriptCodeAnalyzer (full) / IncrementalJavaScriptAnalyzer (PR)
 *
 * One workspace is created, one repository is cloned, one cleanup happens.
 * The rest is routing.
 */
@Service
public class MultiLanguageAnalyzer {

    private final GitHubRepositoryService repositoryService;
    private final RepositoryWorkspaceService workspaceService;
    private final AnalysisLogger logger;

    // Java
    private final JavaCodeAnalyzer javaCodeAnalyzer;
    private final IncrementalJavaAnalyzer incrementalJavaAnalyzer;

    // Python
    private final PythonCodeAnalyzer pythonCodeAnalyzer;
    private final IncrementalPythonAnalyzer incrementalPythonAnalyzer;

    // JavaScript
    private final JavaScriptCodeAnalyzer javaScriptCodeAnalyzer;
    private final IncrementalJavaScriptAnalyzer incrementalJavaScriptAnalyzer;

    public MultiLanguageAnalyzer(
            GitHubRepositoryService repositoryService,
            RepositoryWorkspaceService workspaceService,
            AnalysisLogger logger,
            JavaCodeAnalyzer javaCodeAnalyzer,
            IncrementalJavaAnalyzer incrementalJavaAnalyzer,
            PythonCodeAnalyzer pythonCodeAnalyzer,
            IncrementalPythonAnalyzer incrementalPythonAnalyzer,
            JavaScriptCodeAnalyzer javaScriptCodeAnalyzer,
            IncrementalJavaScriptAnalyzer incrementalJavaScriptAnalyzer
    ) {
        this.repositoryService = repositoryService;
        this.workspaceService = workspaceService;
        this.logger = logger;
        this.javaCodeAnalyzer = javaCodeAnalyzer;
        this.incrementalJavaAnalyzer = incrementalJavaAnalyzer;
        this.pythonCodeAnalyzer = pythonCodeAnalyzer;
        this.incrementalPythonAnalyzer = incrementalPythonAnalyzer;
        this.javaScriptCodeAnalyzer = javaScriptCodeAnalyzer;
        this.incrementalJavaScriptAnalyzer = incrementalJavaScriptAnalyzer;
    }

    public AnalysisResult analyze(
            Long scanId,
            Long projectId,
            String owner,
            String repository,
            String branch,
            Long pullRequestNumber,
            boolean incremental,
            Language language
    ) {
        List<CodeIssue> issues = new ArrayList<>();
        Path workspace = null;

        try {
            logger.started(scanId, repository);
            workspace = workspaceService.createWorkspace(scanId);

            repositoryService.cloneRepository(owner, repository, branch, workspace);

            System.out.println("Analyzing " + language + " repository: " + repository);

            issues.addAll(route(
                    language, incremental, pullRequestNumber,
                    owner, repository, workspace
            ));

            logger.completed(scanId, 0, issues.size());

            return new AnalysisResult(
                    scanId, projectId, owner, repository, pullRequestNumber, issues
            );

        } catch (Exception e) {
            throw new RuntimeException("Multi-language analysis failed for " + language, e);
        } finally {
            if (workspace != null) {
                try {
                    workspaceService.deleteWorkspace(workspace);
                } catch (IOException e) {
                    System.err.println("Failed to cleanup workspace: " + e.getMessage());
                }
            }
        }
    }

    private List<CodeIssue> route(
            Language language,
            boolean incremental,
            Long pullRequestNumber,
            String owner,
            String repository,
            Path workspace
    ) throws Exception {
        if (language == Language.MULTI) {
            List<CodeIssue> multiIssues = new ArrayList<>();
            multiIssues.addAll(routeJava(incremental, pullRequestNumber, owner, repository, workspace));
            multiIssues.addAll(routePython(incremental, pullRequestNumber, owner, repository, workspace));
            multiIssues.addAll(routeJavaScript(incremental, pullRequestNumber, owner, repository, workspace));
            return multiIssues;
        }

        return switch (language) {
            case JAVA -> routeJava(incremental, pullRequestNumber, owner, repository, workspace);
            case PYTHON -> routePython(incremental, pullRequestNumber, owner, repository, workspace);
            case JAVASCRIPT -> routeJavaScript(incremental, pullRequestNumber, owner, repository, workspace);
            default -> new ArrayList<>();
        };
    }

    private List<CodeIssue> routeJava(
            boolean incremental, Long prNumber,
            String owner, String repo, Path workspace
    ) throws Exception {
        if (incremental && prNumber != null) {
            return incrementalJavaAnalyzer.analyze(owner, repo, prNumber, workspace);
        }
        // Delegate full scan to JavaCodeAnalyzer's internal logic via its analyzeWorkspace
        return javaCodeAnalyzer.analyzeWorkspace(workspace);
    }

    private List<CodeIssue> routePython(
            boolean incremental, Long prNumber,
            String owner, String repo, Path workspace
    ) throws Exception {
        if (incremental && prNumber != null) {
            return incrementalPythonAnalyzer.analyze(owner, repo, prNumber, workspace);
        }
        return pythonCodeAnalyzer.analyzeWorkspace(workspace);
    }

    private List<CodeIssue> routeJavaScript(
            boolean incremental, Long prNumber,
            String owner, String repo, Path workspace
    ) throws Exception {
        if (incremental && prNumber != null) {
            return incrementalJavaScriptAnalyzer.analyze(owner, repo, prNumber, workspace);
        }
        return javaScriptCodeAnalyzer.analyzeWorkspace(workspace);
    }
}
