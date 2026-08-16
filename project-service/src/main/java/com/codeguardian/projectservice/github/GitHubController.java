package com.codeguardian.projectservice.github;

import com.codeguardian.projectservice.github.dto.GitHubPullRequestFileResponse;
import com.codeguardian.projectservice.github.dto.GitHubPullRequestResponse;
import com.codeguardian.projectservice.github.dto.GitHubRepositoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService githubService;

    public GitHubController(GitHubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/repositories/{owner}/{repository}")
    public ResponseEntity<GitHubRepositoryResponse> getRepository(
            @PathVariable String owner,
            @PathVariable String repository) {

        return ResponseEntity.ok(
                githubService.getRepository(owner, repository)
        );
    }

    @GetMapping("/repositories/{owner}/{repository}/pulls")
    public ResponseEntity<List<GitHubPullRequestResponse>> getPullRequests(
            @PathVariable String owner,
            @PathVariable String repository) {

        return ResponseEntity.ok(
                githubService.getPullRequests(owner, repository)
        );
    }

    @GetMapping(
            "/repositories/{owner}/{repository}/pulls/{pullNumber}/files"
    )
    public ResponseEntity<List<GitHubPullRequestFileResponse>> getPullRequestFiles(
            @PathVariable String owner,
            @PathVariable String repository,
            @PathVariable int pullNumber) {

        return ResponseEntity.ok(
                githubService.getPullRequestFiles(
                        owner,
                        repository,
                        pullNumber
                )
        );
    }
}
