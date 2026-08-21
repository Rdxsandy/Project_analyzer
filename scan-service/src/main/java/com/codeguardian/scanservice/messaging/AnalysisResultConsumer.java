package com.codeguardian.scanservice.messaging;

import com.codeguardian.scanservice.entity.Scan;
import com.codeguardian.scanservice.entity.ScanStatus;
import com.codeguardian.scanservice.repository.ScanRepository;
import com.codeguardian.scanservice.service.ScanIssueService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AnalysisResultConsumer {

    private final ScanRepository scanRepository;
    private final ScanIssueService scanIssueService;
    private final com.codeguardian.scanservice.service.ScanMetricsService scanMetricsService;

    public AnalysisResultConsumer(
            ScanRepository scanRepository,
            ScanIssueService scanIssueService,
            com.codeguardian.scanservice.service.ScanMetricsService scanMetricsService
    ) {
        this.scanRepository = scanRepository;
        this.scanIssueService = scanIssueService;
        this.scanMetricsService = scanMetricsService;
    }

    @RabbitListener(
            queues = RabbitMQConstants.RESULT_QUEUE
    )
    public void consume(
            AnalysisResultMessage result
    ) {

        System.out.println(
                "Analysis result received: scanId="
                        + result.getScanId()
        );

        Scan scan = scanRepository
                .findById(result.getScanId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Scan not found: "
                                        + result.getScanId()
                        )
                );

        try {

            if (result.getIssues() != null) {

                for (AnalysisIssueMessage issue :
                        result.getIssues()) {

                    scanIssueService.saveIssue(
                            scan,
                            issue
                    );
                }
            }

            scan.setStatus(ScanStatus.COMPLETED);
            scan.setCompletedAt(java.time.LocalDateTime.now());
            
            com.codeguardian.scanservice.dto.ScanMetricsResponse metrics = scanMetricsService.calculate(scan.getId());
            scan.setTotalIssues(metrics.getTotal());
            scan.setCriticalIssues(metrics.getCritical());
            scan.setHighIssues(metrics.getHigh());
            scan.setMediumIssues(metrics.getMedium());
            scan.setLowIssues(metrics.getLow());
            scan.setQualityScore(metrics.getQualityScore());

            scanRepository.save(scan);

            System.out.println(
                    "Scan "
                            + scan.getId()
                            + " completed successfully."
            );

        } catch (Exception e) {

            scan.setStatus(ScanStatus.FAILED);
            scanRepository.save(scan);

            System.err.println(
                    "Scan "
                            + scan.getId()
                            + " failed: "
                            + e.getMessage()
            );
            throw e;
        }
    }
}
