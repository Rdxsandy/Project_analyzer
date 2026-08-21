package com.codeguardian.aireviewservice.ai;

import com.codeguardian.aireviewservice.model.AIReviewedIssue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OpenAIResponseParser {

    private final ObjectMapper objectMapper;

    public OpenAIResponseParser(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public AIReviewedIssue parse(
            String response
    ) throws Exception {

        JsonNode root =
                objectMapper.readTree(response);

        String content =
                root.at(
                        "/choices/0/message/content"
                ).asText();

        content =
                removeMarkdown(content);

        return objectMapper.readValue(
                content,
                AIReviewedIssue.class
        );
    }

    private String removeMarkdown(
            String content
    ) {
        content = content.trim();

        if (content.startsWith("```json")) {
            content = content.substring(7);
        }

        if (content.startsWith("```")) {
            content = content.substring(3);
        }

        if (content.endsWith("```")) {
            content = content.substring(
                    0,
                    content.length() - 3
            );
        }

        return content.trim();
    }
}
