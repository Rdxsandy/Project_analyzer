package com.codeguardian.analyzerservice.analyzer.github;

import com.codeguardian.analyzerservice.analyzer.github.dto.GitHubPullRequestFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class GitHubPullRequestService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String token;

    public GitHubPullRequestService(
            ObjectMapper objectMapper,
            @Value("${github.api-url:https://api.github.com}")
            String apiUrl,

            @Value("${github.token}")
            String token
    ) {
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.token = token;
    }

    public List<GitHubPullRequestFile> getChangedFiles(
            String owner,
            String repository,
            Long pullRequestNumber
    ) throws IOException, InterruptedException {

        List<GitHubPullRequestFile> allFiles = new java.util.ArrayList<>();
        int page = 1;
        int perPage = 100;

        while (true) {
            String url =
                    apiUrl
                            + "/repos/"
                            + owner
                            + "/"
                            + repository
                            + "/pulls/"
                            + pullRequestNumber
                            + "/files"
                            + "?per_page="
                            + perPage
                            + "&page="
                            + page;

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
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "GitHub API failed: "
                                + response.statusCode()
                );
            }

            List<GitHubPullRequestFile> files =
                    objectMapper.readValue(
                            response.body(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<GitHubPullRequestFile>>() {
                            }
                    );

            if (files.isEmpty()) {
                break;
            }

            allFiles.addAll(files);

            if (files.size() < perPage) {
                break;
            }

            page++;
        }

        return allFiles;
    }
}
