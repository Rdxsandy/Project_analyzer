package com.codeguardian.analyzerservice.messaging;

import com.codeguardian.analyzerservice.model.AnalysisResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisResultPublisher {

    private final RabbitTemplate rabbitTemplate;

    public AnalysisResultPublisher(
            RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(AnalysisResult result) {

        rabbitTemplate.convertAndSend(
                "codeguardian.analysis.exchange",
                "analysis.result",
                result
        );

        System.out.println(
                "Analysis result published: scanId="
                        + result.getScanId()
                        + ", issues="
                        + result.getTotalIssues()
        );
    }
}
