package com.codeguardian.analyzerservice.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class JavaFileScanner {

    public List<Path> findJavaFiles(String directory) throws IOException {

        try (Stream<Path> paths = Files.walk(Path.of(directory))) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
        }
    }
}
