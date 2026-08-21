package com.codeguardian.analyzerservice.analyzer.github;

import com.codeguardian.analyzerservice.analyzer.github.dto.GitHubPullRequestFile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChangedJavaFileFilter {

    public List<GitHubPullRequestFile> filter(
            List<GitHubPullRequestFile> files
    ) {

        return files.stream()
                .filter(file ->
                        file.getFilename() != null
                                && file.getFilename()
                                .endsWith(".java"))
                .filter(file ->
                        !"removed".equalsIgnoreCase(
                                file.getStatus()
                        ))
                .toList();
    }
}
