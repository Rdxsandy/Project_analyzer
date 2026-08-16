package com.codeguardian.projectservice.github;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
public class GitHubWebhookController {

    private final GitHubWebhookService webhookService;

    public GitHubWebhookController(
            GitHubWebhookService webhookService) {

        this.webhookService = webhookService;
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

        System.out.println("GitHub event: " + event);

        if ("pull_request".equals(event)) {
            System.out.println("Pull request event received");
        }

        return ResponseEntity.ok("Webhook received");
    }
}
