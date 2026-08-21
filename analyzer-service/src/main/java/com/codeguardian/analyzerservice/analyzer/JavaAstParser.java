package com.codeguardian.analyzerservice.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class JavaAstParser {

    public CompilationUnit parse(Path file) {

        try {
            return StaticJavaParser.parse(file);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Java file: " + file,
                    e
            );
        }
    }
}
