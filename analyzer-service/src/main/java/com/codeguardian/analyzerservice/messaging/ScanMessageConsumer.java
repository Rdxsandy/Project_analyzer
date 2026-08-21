package com.codeguardian.analyzerservice.messaging;

import com.codeguardian.analyzerservice.analyzer.MultiLanguageAnalyzer;
import com.codeguardian.analyzerservice.model.AnalysisResult;
import com.codeguardian.analyzerservice.model.Language;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ScanMessageConsumer {

    private final MultiLanguageAnalyzer multiLanguageAnalyzer;
    private final AnalysisResultPublisher resultPublisher;

    public ScanMessageConsumer(
            MultiLanguageAnalyzer multiLanguageAnalyzer,
            AnalysisResultPublisher resultPublisher
    ) {
        this.multiLanguageAnalyzer = multiLanguageAnalyzer;
        this.resultPublisher = resultPublisher;
    }

    @RabbitListener(queues = RabbitMQConstants.SCAN_QUEUE)
    public void consume(ScanMessage message) {

        Language language = Language.fromString(message.getLanguage());

        System.out.println(
                "Received scan request: scanId="
                        + message.getScanId()
                        + " language=" + language
                        + " incremental=" + message.isIncremental()
        );

        AnalysisResult result = multiLanguageAnalyzer.analyze(
                message.getScanId(),
                message.getProjectId(),
                message.getOwner(),
                message.getRepository(),
                message.getBranch(),
                message.getPullRequestNumber(),
                message.isIncremental(),
                language
        );

        resultPublisher.publish(result);
    }
}
