package com.codeguardian.scanservice.controller;

import com.codeguardian.scanservice.dto.ScanIssueResponse;
import com.codeguardian.scanservice.dto.ScanRequest;
import com.codeguardian.scanservice.dto.ScanResponse;
import com.codeguardian.scanservice.service.ScanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.codeguardian.scanservice.dto.ScanMetricsResponse;
import com.codeguardian.scanservice.service.ScanMetricsService;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService scanService;
    private final ScanMetricsService scanMetricsService;
    private final com.codeguardian.scanservice.repository.AIReviewRepository aiReviewRepository;

    public ScanController(
            ScanService scanService,
            ScanMetricsService scanMetricsService,
            com.codeguardian.scanservice.repository.AIReviewRepository aiReviewRepository
    ) {
        this.scanService = scanService;
        this.scanMetricsService = scanMetricsService;
        this.aiReviewRepository = aiReviewRepository;
    }

    @PostMapping
    public ResponseEntity<ScanResponse> createScan(
            @Valid @RequestBody ScanRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(scanService.createScan(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanResponse> getScan(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                scanService.getScan(id)
        );
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ScanResponse>> getProjectScans(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                scanService.getProjectScans(projectId)
        );
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ScanResponse> startScan(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                scanService.startScan(id)
        );
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ScanResponse> completeScan(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                scanService.completeScan(id)
        );
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<ScanResponse> failScan(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                scanService.failScan(id)
        );
    }

    @GetMapping("/{id}/issues")
    public ResponseEntity<List<ScanIssueResponse>> getScanIssues(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                scanService.getScanIssues(id)
        );
    }

    @GetMapping("/{id}/metrics")
    public ScanMetricsResponse getMetrics(
            @PathVariable Long id
    ) {
        return scanMetricsService.calculate(id);
    }

    @GetMapping("/{id}/ai-reviews")
    public List<com.codeguardian.scanservice.dto.AIReviewResponse> getAIReviews(
            @PathVariable Long id
    ) {
        return aiReviewRepository.findByScan_Id(id).stream()
                .map(r -> new com.codeguardian.scanservice.dto.AIReviewResponse(
                        r.getId(),
                        r.getRule(),
                        r.isValid(),
                        r.getConfidence(),
                        r.getExplanation(),
                        r.getRecommendation()
                ))
                .toList();
    }
}
