package com.codeguardian.analyzerservice.controller;

import com.codeguardian.analyzerservice.analyzer.JavaCodeAnalyzer;
import com.codeguardian.analyzerservice.analyzer.javascript.JavaScriptCodeAnalyzer;
import com.codeguardian.analyzerservice.analyzer.python.PythonCodeAnalyzer;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.model.Language;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST endpoint for the upload-service to request analysis of a
 * local directory that has already been saved to disk.
 *
 * Called by upload-service after it extracts uploaded files.
 * Both services run on the same machine so the path is accessible to both.
 *
 * POST /api/analyze/local
 * Body: { "path": "/tmp/.../upload-xxx", "language": "JAVA" }
 */
@RestController
@RequestMapping("/api/analyze")
public class LocalAnalysisController {

    private final JavaCodeAnalyzer javaCodeAnalyzer;
    private final PythonCodeAnalyzer pythonCodeAnalyzer;
    private final JavaScriptCodeAnalyzer javaScriptCodeAnalyzer;

    public LocalAnalysisController(
            JavaCodeAnalyzer javaCodeAnalyzer,
            PythonCodeAnalyzer pythonCodeAnalyzer,
            JavaScriptCodeAnalyzer javaScriptCodeAnalyzer
    ) {
        this.javaCodeAnalyzer = javaCodeAnalyzer;
        this.pythonCodeAnalyzer = pythonCodeAnalyzer;
        this.javaScriptCodeAnalyzer = javaScriptCodeAnalyzer;
    }

    @PostMapping("/local")
    public ResponseEntity<List<CodeIssue>> analyzeLocal(
            @RequestBody Map<String, String> body
    ) {
        String rawPath = body.get("path");
        String rawLang = body.getOrDefault("language", "JAVA");

        if (rawPath == null || rawPath.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Path workspace = Path.of(rawPath);
        if (!workspace.toFile().exists() || !workspace.toFile().isDirectory()) {
            return ResponseEntity.badRequest().build();
        }

        Language language = Language.fromString(rawLang);
        List<CodeIssue> issues;

        try {
            issues = switch (language) {
                case PYTHON     -> pythonCodeAnalyzer.analyzeWorkspace(workspace);
                case JAVASCRIPT -> javaScriptCodeAnalyzer.analyzeWorkspace(workspace);
                default         -> javaCodeAnalyzer.analyzeWorkspace(workspace);
            };
        } catch (Exception e) {
            System.err.println("[LocalAnalysis] Failed: " + e.getMessage());
            issues = Collections.emptyList();
        }

        return ResponseEntity.ok(issues);
    }
}
