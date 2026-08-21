package com.codeguardian.aireviewservice.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OpenAIClient implements LLMClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public OpenAIClient(
            @Value("${ai.api-url}") String apiUrl,
            @Value("${ai.api-key}") String apiKey,
            @Value("${ai.model}") String model
    ) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String generate(String prompt) {
        try {
            String body =
                    """
                    {
                      "model": "%s",
                      "messages": [
                        {
                          "role": "system",
                          "content": "You are a senior software security engineer."
                        },
                        {
                          "role": "user",
                          "content": %s
                        }
                      ],
                      "temperature": 0.1
                    }
                    """.formatted(
                            model,
                            escapeJson(prompt)
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            apiUrl
                                                    + "/chat/completions"
                                    )
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(body)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "LLM request failed: "
                                + response.statusCode()
                                + " "
                                + response.body()
                );
            }

            return response.body();

        } catch (Exception e) {
            throw new RuntimeException("Error calling OpenAI", e);
        }
    }

    private String escapeJson(String value) {
        return "\""
                + value
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                + "\"";
    }
}
