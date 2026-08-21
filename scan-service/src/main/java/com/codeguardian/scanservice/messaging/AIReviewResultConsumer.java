package com.codeguardian.scanservice.messaging;

import com.codeguardian.scanservice.entity.AIReview;
import com.codeguardian.scanservice.entity.Scan;
import com.codeguardian.scanservice.repository.AIReviewRepository;
import com.codeguardian.scanservice.repository.ScanRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AIReviewResultConsumer {

    private final ScanRepository scanRepository;
    private final AIReviewRepository aiReviewRepository;

    public AIReviewResultConsumer(
            ScanRepository scanRepository,
            AIReviewRepository aiReviewRepository
    ) {
        this.scanRepository = scanRepository;
        this.aiReviewRepository = aiReviewRepository;
    }

    @RabbitListener(queues = RabbitMQConstants.AI_RESULT_QUEUE)
    public void consume(AIReviewResult result) {
        Scan scan = scanRepository.findById(result.getScanId()).orElseThrow();

        if (result.getIssues() != null) {
            for (AIReviewedIssue issue : result.getIssues()) {
                AIReview review = new AIReview();
                review.setScan(scan);
                review.setRule(issue.getRule());
                review.setValid(issue.isValid());
                review.setConfidence(issue.getConfidence());
                review.setExplanation(issue.getExplanation());
                review.setRecommendation(issue.getRecommendation());

                aiReviewRepository.save(review);
            }
        }

        System.out.println("AI review saved for scan " + scan.getId());
    }
}
