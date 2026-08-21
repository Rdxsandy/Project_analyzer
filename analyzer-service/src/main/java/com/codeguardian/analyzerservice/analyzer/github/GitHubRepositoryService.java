package com.codeguardian.analyzerservice.analyzer.github;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class GitHubRepositoryService {

    public Path cloneRepository(
            String owner,
            String repository,
            String branch,
            Path workspace
    ) throws IOException, InterruptedException {

        String url =
                "https://github.com/"
                        + owner
                        + "/"
                        + repository
                        + ".git";

        Process process = new ProcessBuilder(
                "git",
                "clone",
                "--depth",
                "1",
                "--branch",
                branch,
                url,
                workspace.toString()
        )
                .redirectErrorStream(true)
                .start();

        String output =
                new String(
                        process.getInputStream()
                                .readAllBytes()
                );

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    "Git clone failed: " + output
            );
        }

        return workspace;
    }
}
