package com.codeguardian.analyzerservice.analyzer.rule;

import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.model.IssueSeverity;
import com.codeguardian.analyzerservice.model.IssueType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RuntimeExecRule implements JavaRule {

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JAVA-SEC-001",
                "Runtime Command Execution",
                "Detects Runtime.exec() calls that may execute operating-system commands.",
                IssueType.SECURITY,
                IssueSeverity.HIGH
        );
    }

    @Override
    public List<CodeIssue> check(
            String fileName,
            CompilationUnit unit
    ) {

        List<CodeIssue> issues = new ArrayList<>();

        unit.findAll(MethodCallExpr.class)
                .forEach(method -> {

                    if (method.getNameAsString().equals("exec")
                            && method.getScope().isPresent()
                            && method.getScope().get().toString()
                            .equals("Runtime.getRuntime()")) {

                        int line = method.getBegin()
                                .map(position -> position.line)
                                .orElse(0);

                        issues.add(new CodeIssue(
                                fileName,
                                line,
                                IssueType.SECURITY,
                                IssueSeverity.HIGH,
                                "JAVA-SEC-001",
                                "Runtime.exec() can execute operating-system commands.",
                                "Avoid executing shell commands directly. If required, strictly validate and allow-list command arguments."
                        ));
                    }
                });

        return issues;
    }
}
