package com.codeguardian.aireviewservice.ai;

import com.codeguardian.aireviewservice.model.AIReviewRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(
            AIReviewRequest request
    ) {

        return """
                You are a senior software security engineer
                reviewing a static-analysis finding.

                Your task is to determine whether the finding
                represents a genuine software-quality issue.

                File:
                %s

                Line:
                %d

                Rule:
                %s

                Severity:
                %s

                Static analysis message:
                %s

                Source context:
                ```java
                %s
                ```

                Evaluate:

                1. Is this a genuine issue?
                2. Is the static analyzer producing a false positive?
                3. How confident are you?
                4. Why is this code problematic?
                5. What should the developer do?

                Return ONLY valid JSON:

                {
                  "valid": true,
                  "confidence": "HIGH",
                  "explanation": "...",
                  "recommendation": "..."
                }

                Do not return Markdown.
                """.formatted(
                request.getFile(),
                request.getLine(),
                request.getRule(),
                request.getSeverity(),
                request.getMessage(),
                request.getSourceContext()
        );
    }
}
