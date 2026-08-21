package com.codeguardian.analyzerservice.analyzer;

import com.codeguardian.analyzerservice.analyzer.github.ChangedJavaFileFilter;
import com.codeguardian.analyzerservice.analyzer.github.GitHubPullRequestService;
import com.codeguardian.analyzerservice.analyzer.github.dto.GitHubPullRequestFile;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.analyzer.JavaRuleEngine;
import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncrementalJavaAnalyzer {

    private final GitHubPullRequestService pullRequestService;
    private final ChangedJavaFileFilter fileFilter;
    private final JavaAstParser astParser;
    private final JavaRuleEngine ruleEngine;
    private final SourceContextExtractor sourceContextExtractor;

    public IncrementalJavaAnalyzer(
            GitHubPullRequestService pullRequestService,
            ChangedJavaFileFilter fileFilter,
            JavaAstParser astParser,
            JavaRuleEngine ruleEngine,
            SourceContextExtractor sourceContextExtractor
    ) {
        this.pullRequestService = pullRequestService;
        this.fileFilter = fileFilter;
        this.astParser = astParser;
        this.ruleEngine = ruleEngine;
        this.sourceContextExtractor = sourceContextExtractor;
    }

    public List<CodeIssue> analyze(
            String owner,
            String repository,
            Long pullRequestNumber,
            Path workspace
    ) throws Exception {

        List<CodeIssue> issues =
                new ArrayList<>();

        List<GitHubPullRequestFile> changedFiles =
                pullRequestService.getChangedFiles(
                        owner,
                        repository,
                        pullRequestNumber
                );

        List<GitHubPullRequestFile> javaFiles =
                fileFilter.filter(changedFiles);

        System.out.println(
                "Changed files: "
                        + changedFiles.size()
        );

        System.out.println(
                "Changed Java files: "
                        + javaFiles.size()
        );

        for (GitHubPullRequestFile changedFile :
                javaFiles) {

            Path file =
                    workspace.resolve(
                            changedFile.getFilename()
                    );

            if (!Files.exists(file)) {

                System.out.println(
                        "File not found in workspace: "
                                + changedFile.getFilename()
                );

                continue;
            }

            List<String> lines = Files.readAllLines(file);

            CompilationUnit unit =
                    astParser.parse(file);

            List<CodeIssue> fileIssues =
                    ruleEngine.analyze(
                            changedFile.getFilename(),
                            unit
                    );

            for (CodeIssue issue : fileIssues) {
                issue.setSourceContext(
                        sourceContextExtractor.extract(lines, issue.getLine())
                );
            }

            issues.addAll(fileIssues);
        }

        return issues;
    }
}
