package com.codeguardian.projectservice.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class GitHubWebhookService {

    private final String webhookSecret;

    public GitHubWebhookService(
            @Value("${github.webhook-secret}") String webhookSecret) {

        this.webhookSecret = webhookSecret;
    }

    public boolean isValidSignature(
            String payload,
            String signature) {

        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }

        try {
            String expectedSignature = "sha256=" +
                    hmacSha256(payload, webhookSecret);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception exception) {
            return false;
        }
    }

    private String hmacSha256(
            String payload,
            String secret) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        mac.init(secretKey);

        byte[] hash = mac.doFinal(
                payload.getBytes(StandardCharsets.UTF_8)
        );

        StringBuilder result = new StringBuilder();

        for (byte value : hash) {
            result.append(String.format("%02x", value));
        }

        return result.toString();
    }
}
