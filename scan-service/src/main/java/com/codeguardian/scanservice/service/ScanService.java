package com.codeguardian.scanservice.service;

import com.codeguardian.scanservice.client.ProjectServiceClient;
import com.codeguardian.scanservice.dto.ScanIssueResponse;
import com.codeguardian.scanservice.dto.ScanRequest;
import com.codeguardian.scanservice.dto.ScanResponse;
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
