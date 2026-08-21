package com.codeguardian.analyzerservice.analyzer.python;

import com.codeguardian.analyzerservice.analyzer.FileScanner;
import com.codeguardian.analyzerservice.analyzer.github.GitHubPullRequestService;
import com.codeguardian.analyzerservice.analyzer.github.dto.GitHubPullRequestFile;
import com.codeguardian.analyzerservice.model.CodeIssue;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * PR/incremental analysis for Python — filters changed .py files only.
 */
@Service
public class IncrementalPythonAnalyzer {

    private final GitHubPullRequestService pullRequestService;
    private final PythonCodeAnalyzer pythonCodeAnalyzer;
    private final FileScanner fileScanner;

    public IncrementalPythonAnalyzer(
            GitHubPullRequestService pullRequestService,
            PythonCodeAnalyzer pythonCodeAnalyzer,
            FileScanner fileScanner
    ) {
        this.pullRequestService = pullRequestService;
        this.pythonCodeAnalyzer = pythonCodeAnalyzer;
        this.fileScanner = fileScanner;
    }

    public List<CodeIssue> analyze(
            String owner,
            String repository,
            Long pullRequestNumber,
            Path workspace
    ) throws Exception {

        List<GitHubPullRequestFile> changedFiles =
                pullRequestService.getChangedFiles(owner, repository, pullRequestNumber);

        List<Path> pythonFiles = changedFiles.stream()
                .filter(f -> f.getFilename() != null && f.getFilename().endsWith(".py"))
                .filter(f -> !"removed".equalsIgnoreCase(f.getStatus()))
                .map(f -> workspace.resolve(f.getFilename()))
                .filter(Files::exists)
                .toList();

        System.out.println("Changed Python files: " + pythonFiles.size());

        return pythonCodeAnalyzer.analyzeFiles(pythonFiles, workspace);
    }
}
