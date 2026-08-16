package com.codeguardian.projectservice.github;

import com.codeguardian.projectservice.github.dto.GitHubPullRequestFileResponse;
import com.codeguardian.projectservice.github.dto.GitHubPullRequestResponse;
import com.codeguardian.projectservice.github.dto.GitHubRepositoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class GitHubService {

    private final RestClient githubRestClient;

    public GitHubService(RestClient githubRestClient) {
        this.githubRestClient = githubRestClient;
    }

    public GitHubRepositoryResponse getRepository(
            String owner,
            String repository) {

        return githubRestClient
                .get()
                .uri("/repos/{owner}/{repo}", owner, repository)
                .retrieve()
                .body(GitHubRepositoryResponse.class);
    }

    public List<GitHubPullRequestResponse> getPullRequests(
            String owner,
            String repository) {

        GitHubPullRequestResponse[] response = githubRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/pulls")
                        .queryParam("state", "open")
                        .queryParam("per_page", 100)
                        .build(owner, repository))
                .retrieve()
                .body(GitHubPullRequestResponse[].class);

        return response == null
                ? List.of()
                : Arrays.asList(response);
    }

    public List<GitHubPullRequestFileResponse> getPullRequestFiles(
            String owner,
            String repository,
            int pullNumber) {

        GitHubPullRequestFileResponse[] response = githubRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/pulls/{pullNumber}/files")
                        .queryParam("per_page", 100)
                        .build(owner, repository, pullNumber))
                .retrieve()
                .body(GitHubPullRequestFileResponse[].class);

        return response == null
                ? List.of()
                : Arrays.asList(response);
    }
}
