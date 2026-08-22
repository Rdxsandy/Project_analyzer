package com.codeguardian.uploadservice.controller;

import com.codeguardian.uploadservice.dto.UploadScanResponse;
import com.codeguardian.uploadservice.service.UploadScanService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Accepts multipart file uploads from the frontend.
 *
 * POST /api/upload/scan
 *   files[]     – one or more .java / .py / .js / .ts / .jsx / .tsx / .zip
 *   language    – JAVA | PYTHON | JAVASCRIPT
 *   projectId   – optional, associates result with an existing project
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadScanService uploadScanService;

    public UploadController(UploadScanService uploadScanService) {
        this.uploadScanService = uploadScanService;
    }

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadScanResponse> scan(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "language", defaultValue = "JAVA") String language,
            @RequestParam(value = "projectId", required = false) Long projectId
    ) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            UploadScanResponse result = uploadScanService.scan(files, language, projectId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Upload scan failed: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("upload-service OK");
    }
}
