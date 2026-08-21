package com.codeguardian.scanservice.service;

import com.codeguardian.scanservice.entity.Scan;
import com.codeguardian.scanservice.entity.ScanIssue;
import com.codeguardian.scanservice.messaging.AnalysisIssueMessage;
import com.codeguardian.scanservice.repository.ScanIssueRepository;
import org.springframework.stereotype.Service;

@Service
public class ScanIssueService {

    private final ScanIssueRepository scanIssueRepository;

    public ScanIssueService(
            ScanIssueRepository scanIssueRepository
    ) {
        this.scanIssueRepository = scanIssueRepository;
    }

    public ScanIssue saveIssue(
            Scan scan,
            AnalysisIssueMessage message
    ) {

        ScanIssue issue = new ScanIssue();

        issue.setScan(scan);
        issue.setFilePath(message.getFile());
        issue.setLineNumber(message.getLine());
        issue.setType(message.getType());
        issue.setSeverity(message.getSeverity());
        issue.setRuleId(message.getRule());
        issue.setMessage(message.getMessage());
        issue.setSuggestion(
                message.getRecommendation()
        );

        return scanIssueRepository.save(issue);
    }
}
