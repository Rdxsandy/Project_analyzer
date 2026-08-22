package com.codeguardian.scanservice.service;

import com.codeguardian.scanservice.client.ProjectServiceClient;
import com.codeguardian.scanservice.dto.ScanIssueResponse;
import com.codeguardian.scanservice.dto.ScanRequest;
import com.codeguardian.scanservice.dto.ScanResponse;
import com.codeguardian.scanservice.entity.IssueSeverity;
import com.codeguardian.scanservice.entity.IssueType;
import com.codeguardian.scanservice.entity.Scan;
import com.codeguardian.scanservice.entity.ScanIssue;
import com.codeguardian.scanservice.entity.ScanStatus;
import com.codeguardian.scanservice.exception.ScanNotFoundException;
import com.codeguardian.scanservice.repository.ScanIssueRepository;
import com.codeguardian.scanservice.repository.ScanRepository;
import com.codeguardian.scanservice.messaging.ScanMessage;
import com.codeguardian.scanservice.messaging.ScanMessagePublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScanService {

    private final ScanRepository scanRepository;
    private final ScanIssueRepository scanIssueRepository;
    private final ProjectServiceClient projectServiceClient;
    private final ScanMessagePublisher scanMessagePublisher;

    public ScanService(ScanRepository scanRepository, ScanIssueRepository scanIssueRepository, ProjectServiceClient projectServiceClient, ScanMessagePublisher scanMessagePublisher) {
        this.scanRepository = scanRepository;
        this.scanIssueRepository = scanIssueRepository;
        this.projectServiceClient = projectServiceClient;
        this.scanMessagePublisher = scanMessagePublisher;
    }

    public ScanResponse createScan(ScanRequest request) {
        // Verify the project exists in project-service before creating the scan
        projectServiceClient.verifyProjectExists(request.getProjectId());

        Scan scan = Scan.builder()
                .projectId(request.getProjectId())
                .repositoryOwner(request.getRepositoryOwner())
                .repositoryName(request.getRepositoryName())
                .pullRequestNumber(request.getPullRequestNumber())
                .status(ScanStatus.PENDING)
                .commitSha(request.getCommitSha())
                .totalFiles(0)
                .totalIssues(0)
                .criticalIssues(0)
                .highIssues(0)
                .mediumIssues(0)
                .lowIssues(0)
                .createdAt(LocalDateTime.now())
                .build();
                
        Scan savedScan = scanRepository.save(scan);

        boolean isIncremental = savedScan.getPullRequestNumber() != null;
        ScanMessage message = new ScanMessage(
                savedScan.getId(),
                savedScan.getProjectId(),
                savedScan.getRepositoryOwner(),
                savedScan.getRepositoryName(),
                "main", // Default branch since it's not stored in Scan entity
                savedScan.getPullRequestNumber() != null ? Long.valueOf(savedScan.getPullRequestNumber()) : null,
                isIncremental
        );

        // Forward language — defaults to JAVA for backwards compatibility
        message.setLanguage(
                request.getLanguage() != null ? request.getLanguage() : "JAVA"
        );

        scanMessagePublisher.publish(message);
        
        savedScan.setStatus(ScanStatus.RUNNING);
        savedScan.setStartedAt(LocalDateTime.now());
        savedScan = scanRepository.save(savedScan);

        return toResponse(savedScan);
    }

    public ScanResponse getScan(Long id) {
        return toResponse(getScanEntity(id));
    }

    public List<ScanResponse> getProjectScans(Long projectId) {
        return scanRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ScanResponse startScan(Long id) {
        Scan scan = getScanEntity(id);
        scan.setStatus(ScanStatus.RUNNING);
        scan.setStartedAt(LocalDateTime.now());
        return toResponse(scanRepository.save(scan));
    }

    public ScanResponse completeScan(Long id) {

        Scan scan = getScanEntity(id);

        scan.setStatus(ScanStatus.COMPLETED);
        scan.setCompletedAt(LocalDateTime.now());

        return toResponse(scanRepository.save(scan));
    }

    public ScanResponse failScan(Long id) {

        Scan scan = getScanEntity(id);

        scan.setStatus(ScanStatus.FAILED);
        scan.setCompletedAt(LocalDateTime.now());

        return toResponse(scanRepository.save(scan));
    }

    public List<ScanIssueResponse> getScanIssues(Long scanId) {

        getScanEntity(scanId);

        return scanIssueRepository.findByScanId(scanId)
                .stream()
                .map(this::toIssueResponse)
                .toList();
    }

    public void saveBulkIssues(Long scanId, List<com.codeguardian.scanservice.dto.BulkIssueRequest> requests) {
        Scan scan = getScanEntity(scanId);

        long critical = 0, high = 0, medium = 0, low = 0;

        for (com.codeguardian.scanservice.dto.BulkIssueRequest req : requests) {

            // Parse severity — default LOW if unknown
            IssueSeverity severity;
            try {
                severity = IssueSeverity.valueOf(req.getSeverity().toUpperCase());
            } catch (Exception e) {
                severity = IssueSeverity.LOW;
            }

            // Parse type — default BUG if unknown
            IssueType type;
            try {
                type = IssueType.valueOf(req.getType().toUpperCase());
            } catch (Exception e) {
                type = IssueType.BUG;
            }

            String message = req.getMessage();
            if (message != null && message.length() > 2000) {
                message = message.substring(0, 1997) + "...";
            }

            ScanIssue issue = ScanIssue.builder()
                    .scan(scan)
                    .severity(severity)
                    .type(type)
                    .message(message != null ? message : "No message")
                    .filePath(req.getFilePath())
                    .lineNumber(req.getLineNumber())
                    .ruleId(req.getRuleId())
                    .suggestion(req.getSuggestion())
                    .build();

            scanIssueRepository.save(issue);

            switch (severity) {
                case CRITICAL -> critical++;
                case HIGH     -> high++;
                case MEDIUM   -> medium++;
                case LOW      -> low++;
            }
        }

        // Update scan counters
        scan.setTotalIssues((int)(scan.getTotalIssues() + requests.size()));
        scan.setCriticalIssues((int)(scan.getCriticalIssues() + critical));
        scan.setHighIssues((int)(scan.getHighIssues() + high));
        scan.setMediumIssues((int)(scan.getMediumIssues() + medium));
        scan.setLowIssues((int)(scan.getLowIssues() + low));
        scanRepository.save(scan);
    }

    private Scan getScanEntity(Long id) {

        return scanRepository.findById(id)
                .orElseThrow(() ->
                        new ScanNotFoundException(
                                "Scan not found: " + id
                        ));
    }

    private ScanResponse toResponse(Scan scan) {

        return new ScanResponse(
                scan.getId(),
                scan.getProjectId(),
                scan.getRepositoryOwner(),
                scan.getRepositoryName(),
                scan.getPullRequestNumber(),
                scan.getStatus(),
                scan.getCommitSha(),
                scan.getTotalFiles(),
                scan.getTotalIssues(),
                scan.getCriticalIssues(),
                scan.getHighIssues(),
                scan.getMediumIssues(),
                scan.getLowIssues(),
                scan.getQualityScore(),
                scan.getCreatedAt(),
                scan.getStartedAt(),
                scan.getCompletedAt()
        );
    }

    private ScanIssueResponse toIssueResponse(ScanIssue issue) {

        return new ScanIssueResponse(
                issue.getId(),
                issue.getSeverity(),
                issue.getType(),
                issue.getMessage(),
                issue.getFilePath(),
                issue.getLineNumber(),
                issue.getRuleId(),
                issue.getSuggestion()
        );
    }
}

