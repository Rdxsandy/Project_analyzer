package com.codeguardian.aireviewservice.ai;

import com.codeguardian.aireviewservice.model.AIReviewRequest;
import com.codeguardian.aireviewservice.model.AIReviewedIssue;
import org.springframework.stereotype.Service;

@Service
public class OpenAIProvider implements AIProvider {

    private final PromptBuilder promptBuilder;
    private final LLMClient llmClient;
    private final OpenAIResponseParser parser;

    public OpenAIProvider(
            PromptBuilder promptBuilder,
            LLMClient llmClient,
            OpenAIResponseParser parser
    ) {
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        this.parser = parser;
    }

    @Override
    public AIReviewedIssue review(
            AIReviewRequest request
    ) {
        try {
            String prompt = promptBuilder.build(request);
            String response = llmClient.generate(prompt);
            
            AIReviewedIssue result = parser.parse(response);
            result.setRule(request.getRule());
            return result;
            
        } catch (Exception e) {
            System.err.println("AI review failed: " + e.getMessage());
            
            AIReviewedIssue fallback = new AIReviewedIssue();
            fallback.setRule(request.getRule());
            fallback.setValid(false);
            fallback.setConfidence("UNKNOWN");
            fallback.setExplanation("AI review failed: " + e.getMessage());
            fallback.setRecommendation("Review manually.");
            return fallback;
        }
    }
}
