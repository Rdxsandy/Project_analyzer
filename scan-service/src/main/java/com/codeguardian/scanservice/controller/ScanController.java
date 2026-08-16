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

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
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
}
