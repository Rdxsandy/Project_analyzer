package com.codeguardian.analyzerservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.codeguardian.analyzerservice.analyzer.CodeAnalyzer;
import com.codeguardian.analyzerservice.model.AnalysisResult;

@Service
public class ScanMessageConsumer {

    private final CodeAnalyzer codeAnalyzer;
    private final AnalysisResultPublisher resultPublisher;

    public ScanMessageConsumer(
            CodeAnalyzer codeAnalyzer,
            AnalysisResultPublisher resultPublisher
    ) {
        this.codeAnalyzer = codeAnalyzer;
        this.resultPublisher = resultPublisher;
    }

    @RabbitListener(
            queues = RabbitMQConstants.SCAN_QUEUE
    )
    public void consume(ScanMessage message) {

        AnalysisResult result =
                codeAnalyzer.analyze(
                        message.getScanId(),
                        message.getProjectId(),
                        message.getOwner(),
                        message.getRepository(),
                        message.getBranch(),
                        message.getPullRequestNumber(),
                        message.isIncremental()
        );

        resultPublisher.publish(result);
    }
}
