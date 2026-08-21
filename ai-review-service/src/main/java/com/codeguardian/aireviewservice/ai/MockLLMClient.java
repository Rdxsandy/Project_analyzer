package com.codeguardian.aireviewservice.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockLLMClient implements LLMClient {

    @Override
    public String generate(String prompt) {
        return """
                {
                  "valid": true,
                  "confidence": "HIGH",
                  "explanation": "The code pattern matches the reported issue.",
                  "recommendation": "Review and apply the recommended secure implementation."
                }
                """;
    }
}
