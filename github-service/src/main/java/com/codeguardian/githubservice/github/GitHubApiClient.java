package com.codeguardian.githubservice.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GitHubApiClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String apiUrl;
    private final String token;

    public GitHubApiClient(
            @Value("${github.api-url:https://api.github.com}") String apiUrl,
            @Value("${github.token}") String token
    ) {
        this.apiUrl = apiUrl;
        this.token = token;
    }

    public void createPullRequestComment(
            String owner,
            String repository,
            Long pullRequestNumber,
            String body
    ) throws Exception {

        String url =
                apiUrl
                        + "/repos/"
                        + owner
                        + "/"
                        + repository
                        + "/issues/"
                        + pullRequestNumber
                        + "/comments";

        String json =
                """
                {
                    "body": %s
                }
                """.formatted(
                        escapeJson(body)
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .header(
                                "Accept",
                                "application/vnd.github+json"
                        )
                        .header(
                                "X-GitHub-Api-Version",
                                "2022-11-28"
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(json)
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
                    "GitHub comment failed: "
                            + response.statusCode()
                            + " "
                            + response.body()
            );
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
