package com.codeguardian.analyzerservice.analyzer;

import com.codeguardian.analyzerservice.analyzer.github.GitHubPullRequestService;
import com.codeguardian.analyzerservice.analyzer.github.GitHubRepositoryService;
import com.codeguardian.analyzerservice.analyzer.github.RepositoryWorkspaceService;
import com.codeguardian.analyzerservice.analyzer.github.dto.GitHubPullRequestFile;
import com.codeguardian.analyzerservice.model.AnalysisResult;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class JavaCodeAnalyzer implements CodeAnalyzer {

    private final JavaFileScanner fileScanner;
    private final JavaAstParser astParser;
    private final JavaRuleEngine ruleEngine;
    private final GitHubRepositoryService repositoryService;
    private final RepositoryWorkspaceService workspaceService;
    private final IncrementalJavaAnalyzer incrementalJavaAnalyzer;
    private final AnalysisLogger logger;
    private final SourceContextExtractor sourceContextExtractor;

    public JavaCodeAnalyzer(
            JavaFileScanner fileScanner,
            JavaAstParser astParser,
            JavaRuleEngine ruleEngine,
            GitHubRepositoryService repositoryService,
            RepositoryWorkspaceService workspaceService,
            IncrementalJavaAnalyzer incrementalJavaAnalyzer,
            AnalysisLogger logger,
            SourceContextExtractor sourceContextExtractor
    ) {
        this.fileScanner = fileScanner;
        this.astParser = astParser;
        this.ruleEngine = ruleEngine;
        this.repositoryService = repositoryService;
        this.workspaceService = workspaceService;
        this.incrementalJavaAnalyzer = incrementalJavaAnalyzer;
        this.logger = logger;
        this.sourceContextExtractor = sourceContextExtractor;
    }

    @Override
    public AnalysisResult analyze(
            Long scanId,
            Long projectId,
            String owner,
            String repository,
            String branch,
            Long pullRequestNumber,
            boolean incremental
    ) {
        List<CodeIssue> issues = new ArrayList<>();
        Path workspace = null;

        try {
            logger.started(scanId, repository);
            workspace = workspaceService.createWorkspace(scanId);

            repositoryService.cloneRepository(
                    owner,
                    repository,
                    branch,
                    workspace
            );

            if (incremental && pullRequestNumber != null) {
                issues.addAll(
                        incrementalJavaAnalyzer.analyze(
                                owner,
                                repository,
                                pullRequestNumber,
                                workspace
                        )
                );
            } else {
                List<Path> files = fileScanner.findJavaFiles(workspace.toString());
                
                for (Path file : files) {
                    List<String> lines = java.nio.file.Files.readAllLines(file);
                    CompilationUnit unit = astParser.parse(file);
                    String relativeFile = workspace.relativize(file).toString();
                    
                    List<CodeIssue> fileIssues = ruleEngine.analyze(relativeFile, unit);
                    
                    for (CodeIssue issue : fileIssues) {
                        issue.setSourceContext(
                                sourceContextExtractor.extract(lines, issue.getLine())
                        );
                    }
                    
                    issues.addAll(fileIssues);
                }
            }

            logger.completed(scanId, 0, issues.size()); // File count logging simplified for now

            return new AnalysisResult(
                    scanId,
                    projectId,
                    owner,
                    repository,
                    pullRequestNumber,
                    issues
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Repository analysis failed",
                    e
            );
        } finally {
            if (workspace != null) {
                try {
                    workspaceService.deleteWorkspace(workspace);
                } catch (IOException e) {
                    System.err.println(
                            "Failed to cleanup workspace: " + e.getMessage()
                    );
                }
            }
        }
    }
}
