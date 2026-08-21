package com.codeguardian.analyzerservice.analyzer.rule;

import com.codeguardian.analyzerservice.model.CodeIssue;
import com.codeguardian.analyzerservice.model.IssueSeverity;
import com.codeguardian.analyzerservice.model.IssueType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmptyCatchRule implements JavaRule {

    @Override
    public RuleMetadata metadata() {
        return new RuleMetadata(
                "JAVA-BUG-001",
                "Empty Catch Block",
                "Detects empty catch blocks that silently suppress exceptions.",
                IssueType.BUG,
                IssueSeverity.MEDIUM
        );
    }

    @Override
    public List<CodeIssue> check(
            String fileName,
            CompilationUnit unit
    ) {

        List<CodeIssue> issues = new ArrayList<>();

        unit.findAll(CatchClause.class)
                .forEach(catchClause -> {

                    if (catchClause.getBody().getStatements().isEmpty()) {

                        int line = catchClause.getBegin()
                                .map(position -> position.line)
                                .orElse(0);

                        issues.add(new CodeIssue(
                                fileName,
                                line,
                                IssueType.BUG,
                                IssueSeverity.MEDIUM,
                                "JAVA-BUG-001",
                                "Empty catch block hides exceptions.",
                                "Handle the exception appropriately or log it with sufficient context."
                        ));
                    }
                });

        return issues;
    }
}
