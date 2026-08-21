package com.codeguardian.analyzerservice.analyzer.rule;

import com.codeguardian.analyzerservice.model.CodeIssue;
import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public interface JavaRule {

    RuleMetadata metadata();

    List<CodeIssue> check(
            String fileName,
            CompilationUnit unit
    );
}
