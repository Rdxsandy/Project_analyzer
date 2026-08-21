package com.codeguardian.scanservice.service;

import com.codeguardian.scanservice.dto.ScanMetricsResponse;
import com.codeguardian.scanservice.entity.IssueSeverity;
import com.codeguardian.scanservice.entity.ScanIssue;
import com.codeguardian.scanservice.repository.ScanIssueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScanMetricsService {

    private final ScanIssueRepository scanIssueRepository;

    public ScanMetricsService(
            ScanIssueRepository scanIssueRepository
    ) {
        this.scanIssueRepository = scanIssueRepository;
    }

    public ScanMetricsResponse calculate(Long scanId) {

        List<ScanIssue> issues = scanIssueRepository.findByScanId(scanId);

        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;

        for (ScanIssue issue : issues) {

            if (issue.getSeverity() == IssueSeverity.CRITICAL) {
                critical++;
            } else if (issue.getSeverity() == IssueSeverity.HIGH) {
                high++;
            } else if (issue.getSeverity() == IssueSeverity.MEDIUM) {
                medium++;
            } else if (issue.getSeverity() == IssueSeverity.LOW) {
                low++;
            }
        }

        int penalty = critical * 10
                + high * 6
                + medium * 3
                + low;

        int qualityScore = Math.max(0, 100 - penalty);

        return new ScanMetricsResponse(
                critical,
                high,
                medium,
                low,
                qualityScore
        );
    }
}
