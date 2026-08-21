package com.codeguardian.analyzerservice.analyzer.rule;

import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.model.IssueSeverity;
import com.codeguardian.analyzerservice.model.IssueType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HardcodedPasswordRule implements JavaRule {

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JAVA-SEC-002",
                "Hardcoded Password",
                "Detects hardcoded passwords in variable declarations.",
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

        unit.findAll(VariableDeclarator.class)
                .forEach(var -> {
                    String name = var.getNameAsString().toLowerCase();
                    if (name.contains("password") && var.getInitializer().isPresent() && var.getInitializer().get().isStringLiteralExpr()) {
                        int line = var.getBegin()
                                .map(position -> position.line)
                                .orElse(0);

                        issues.add(new CodeIssue(
                                fileName,
                                line,
                                IssueType.SECURITY,
                                IssueSeverity.HIGH,
                                "JAVA-SEC-002",
                                "Hardcoded password detected.",
                                "Do not hardcode passwords. Use environment variables or a secure vault."
                        ));
                    }
                });

        return issues;
    }
}
