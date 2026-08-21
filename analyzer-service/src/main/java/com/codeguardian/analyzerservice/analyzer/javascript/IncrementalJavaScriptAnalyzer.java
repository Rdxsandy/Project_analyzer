package com.codeguardian.analyzerservice.analyzer.javascript;

import com.codeguardian.analyzerservice.analyzer.FileScanner;
import com.codeguardian.analyzerservice.analyzer.github.GitHubPullRequestService;
import com.codeguardian.analyzerservice.analyzer.github.dto.GitHubPullRequestFile;
import com.codeguardian.analyzerservice.model.CodeIssue;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * PR/incremental analysis for JavaScript/TypeScript.
 * Filters changed .js, .ts, .jsx, .tsx files only.
 */
@Service
public class IncrementalJavaScriptAnalyzer {

    private static final Set<String> JS_EXTENSIONS = Set.of(".js", ".ts", ".jsx", ".tsx");

    private final GitHubPullRequestService pullRequestService;
    private final JavaScriptCodeAnalyzer jsAnalyzer;

    public IncrementalJavaScriptAnalyzer(
            GitHubPullRequestService pullRequestService,
            JavaScriptCodeAnalyzer jsAnalyzer
    ) {
        this.pullRequestService = pullRequestService;
        this.jsAnalyzer = jsAnalyzer;
    }

    public List<CodeIssue> analyze(
            String owner,
            String repository,
            Long pullRequestNumber,
            Path workspace
    ) throws Exception {

        List<GitHubPullRequestFile> changedFiles =
                pullRequestService.getChangedFiles(owner, repository, pullRequestNumber);

        List<Path> jsFiles = changedFiles.stream()
                .filter(f -> f.getFilename() != null && hasJsExtension(f.getFilename()))
                .filter(f -> !"removed".equalsIgnoreCase(f.getStatus()))
                .map(f -> workspace.resolve(f.getFilename()))
                .filter(Files::exists)
                .toList();

        System.out.println("Changed JS/TS files: " + jsFiles.size());

        return jsAnalyzer.analyzeFiles(jsFiles, workspace);
    }

    private boolean hasJsExtension(String filename) {
        for (String ext : JS_EXTENSIONS) {
            if (filename.endsWith(ext)) return true;
        }
        return false;
    }
}
