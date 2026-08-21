package com.codeguardian.analyzerservice.analyzer.github;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Service
public class RepositoryWorkspaceService {

    private final Path baseDirectory =
            Path.of("analyzer-workspace");

    public Path createWorkspace(
            Long scanId
    ) throws IOException {

        Files.createDirectories(baseDirectory);

        Path workspace =
                baseDirectory.resolve(
                        "scan-" + scanId
                );

        if (Files.exists(workspace)) {
            deleteWorkspace(workspace);
        }

        Files.createDirectories(workspace);

        return workspace;
    }

    public void deleteWorkspace(
            Path workspace
    ) throws IOException {

        if (!Files.exists(workspace)) {
            return;
        }

        try (var paths = Files.walk(workspace)) {

            paths.sorted(
                    Comparator.reverseOrder()
            ).forEach(path -> {

                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(
                            "Failed to delete: " + path,
                            e
                    );
                }
            });
        }
    }
}
