package com.codeguardian.aireviewservice.messaging;

import com.codeguardian.aireviewservice.ai.AIProvider;
import com.codeguardian.aireviewservice.model.AIReviewRequest;
import com.codeguardian.aireviewservice.model.AIReviewedIssue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIReviewConsumer {

    private final AIProvider aiProvider;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public AIReviewConsumer(
            AIProvider aiProvider,
            org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate
    ) {
        this.aiProvider = aiProvider;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(
            queues = RabbitMQConstants.AI_REVIEW_QUEUE
    )
    public void consume(
            StaticAnalysisResult result
    ) {

        System.out.println(
                "AI Review received scan "
                        + result.getScanId()
        );

        List<AIReviewedIssue> reviewed =
                new ArrayList<>();

        if (result.getIssues() != null) {

            for (StaticIssue issue :
                    result.getIssues()) {

                AIReviewRequest request =
                        new AIReviewRequest();

                request.setScanId(
                        result.getScanId()
                );

                request.setProjectId(
                        result.getProjectId()
                );

                request.setFile(
                        issue.getFile()
                );

                request.setLine(
                        issue.getLine()
                );

                request.setRule(
                        issue.getRule()
                );

                request.setSeverity(
                        issue.getSeverity()
                );

                request.setMessage(
                        issue.getMessage()
                );

                request.setSourceContext(
                        issue.getSourceContext()
                );

                AIReviewedIssue review =
                        aiProvider.review(
                                request
                        );

                reviewed.add(review);
            }
        }

        System.out.println(
                "AI reviewed "
                        + reviewed.size()
                        + " issues."
        );

        com.codeguardian.aireviewservice.model.AIReviewResult aiResult =
                new com.codeguardian.aireviewservice.model.AIReviewResult();
        
        aiResult.setScanId(result.getScanId());
        aiResult.setProjectId(result.getProjectId());
        aiResult.setIssues(reviewed);

        rabbitTemplate.convertAndSend(
                "codeguardian.ai.exchange",
                "ai.result",
                aiResult
        );
    }
}
