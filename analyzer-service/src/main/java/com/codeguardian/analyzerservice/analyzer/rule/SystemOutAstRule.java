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
public class SystemOutAstRule implements JavaRule {

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JAVA-SMELL-001",
                "System Out Usage",
                "Detects direct usage of System.out.println in production code.",
                IssueType.CODE_SMELL,
                IssueSeverity.LOW
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

                    if (method.getNameAsString().equals("println")
                            && method.getScope().isPresent()
                            && method.getScope().get().toString()
                            .equals("System.out")) {

                        int line = method.getBegin()
                                .map(position -> position.line)
                                .orElse(0);

                        issues.add(new CodeIssue(
                                fileName,
                                line,
                                IssueType.CODE_SMELL,
                                IssueSeverity.LOW,
                                "JAVA-SMELL-001",
                                "System.out.println should not be used in production code.",
                                "Use a logging framework such as SLF4J instead."
                        ));
                    }
                });

        return issues;
    }
}
