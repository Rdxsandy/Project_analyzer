package com.codeguardian.analyzerservice.analyzer;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Generic file scanner that:
 *  - Filters by file extensions
 *  - Skips vendor/generated directories
 *  - Enforces a per-file size limit (500 KB) to prevent OOM
 *  - Reads files with UTF-8 (falls back to ISO-8859-1)
 */
@Service
public class FileScanner {

    private static final long MAX_FILE_BYTES = 512_000L; // 500 KB

    private static final Set<String> SKIP_DIRS = Set.of(
            "node_modules", "__pycache__", ".venv", "venv", "env",
            ".git", "build", "dist", "target", ".gradle", ".idea",
            "vendor", ".mypy_cache", ".pytest_cache", "coverage"
    );

    /**
     * Recursively find all files with one of the given extensions,
     * skipping vendor/generated directories and files > MAX_FILE_BYTES.
     */
    public List<Path> findFiles(String directory, String... extensions) throws IOException {
        Set<String> extSet = Set.of(extensions);

        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> !isInSkippedDirectory(p))
                    .filter(p -> hasExtension(p, extSet))
                    .filter(p -> {
                        try {
                            return Files.size(p) <= MAX_FILE_BYTES;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .toList();
        }
    }

    /**
     * Read all lines from a file, trying UTF-8 first, then ISO-8859-1.
     */
    public List<String> readLines(Path file) {
        for (Charset charset : Arrays.asList(
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1
        )) {
            try {
                return Files.readAllLines(file, charset);
            } catch (IOException ignored) {
                // try next charset
            }
        }
        return Collections.emptyList();
    }

    private boolean isInSkippedDirectory(Path path) {
        for (Path part : path) {
            if (SKIP_DIRS.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasExtension(Path path, Set<String> extensions) {
        String name = path.getFileName().toString();
        for (String ext : extensions) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
