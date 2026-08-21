package com.codeguardian.projectservice.github;

import com.codeguardian.projectservice.client.ScanRequest;
import com.codeguardian.projectservice.client.ScanServiceClient;
import com.codeguardian.projectservice.entity.Project;
import com.codeguardian.projectservice.repository.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/github")
public class GitHubWebhookController {

    private final GitHubWebhookService webhookService;
    private final ProjectRepository projectRepository;
    private final ScanServiceClient scanServiceClient;
    private final ObjectMapper objectMapper;

    public GitHubWebhookController(
            GitHubWebhookService webhookService,
            ProjectRepository projectRepository,
            ScanServiceClient scanServiceClient,
            ObjectMapper objectMapper
    ) {
        this.webhookService = webhookService;
        this.projectRepository = projectRepository;
        this.scanServiceClient = scanServiceClient;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false)
            String signature,

            @RequestHeader(value = "X-GitHub-Event", required = false)
            String event,

            @RequestBody String payload) {

        if (!webhookService.isValidSignature(payload, signature)) {
            return ResponseEntity
                    .status(401)
                    .body("Invalid webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(payload);

            if ("pull_request".equals(event)) {
                String action = root.path("action").asText();

                if ("opened".equals(action) || "synchronize".equals(action)) {
                    String repositoryUrl = root.path("repository").path("clone_url").asText();
                    String owner = root.path("repository").path("owner").path("login").asText();
                    String repoName = root.path("repository").path("name").asText();
                    Integer prNumber = root.path("pull_request").path("number").asInt();
                    String headSha = root.path("pull_request").path("head").path("sha").asText();

                    Optional<Project> projectOpt = projectRepository.findByRepositoryUrl(repositoryUrl);
                    
                    if (projectOpt.isPresent()) {
                        Project project = projectOpt.get();

                        ScanRequest scanRequest = new ScanRequest();
                        scanRequest.setProjectId(project.getId());
                        scanRequest.setRepositoryOwner(owner);
                        scanRequest.setRepositoryName(repoName);
                        scanRequest.setPullRequestNumber(prNumber);
                        scanRequest.setCommitSha(headSha);

                        scanServiceClient.createScan(scanRequest);
                    } else {
                        System.out.println("Project not found for repository: " + repositoryUrl);
                    }
                }
            }
            
            return ResponseEntity.ok("Webhook received");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
}
