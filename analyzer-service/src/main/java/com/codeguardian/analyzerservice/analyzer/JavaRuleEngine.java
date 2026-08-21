package com.codeguardian.analyzerservice.analyzer;

import com.codeguardian.analyzerservice.analyzer.rule.JavaRule;
import com.codeguardian.analyzerservice.model.CodeIssue;
import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JavaRuleEngine {

    private final List<JavaRule> rules;

    public JavaRuleEngine(List<JavaRule> rules) {
        this.rules = rules;
    }

    public List<CodeIssue> analyze(
            String fileName,
            CompilationUnit unit
    ) {

        List<CodeIssue> issues = new ArrayList<>();

        for (JavaRule rule : rules) {

            System.out.println(
                    "Running rule: "
                            + rule.metadata().id()
                            + " - "
                            + rule.metadata().name()
            );

            issues.addAll(
                    rule.check(fileName, unit)
            );
        }

        return issues;
    }
}
