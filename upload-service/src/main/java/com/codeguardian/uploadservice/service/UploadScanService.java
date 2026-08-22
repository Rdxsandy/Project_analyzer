package com.codeguardian.uploadservice.service;

import com.codeguardian.uploadservice.dto.UploadScanResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Core upload scan orchestration:
 *  1. Save uploaded files to a temp directory (handles .zip extraction too)
 *  2. Call analyzer-service /api/analyze/local   → get issues
 *  3. Call scan-service  /api/scans              → create scan record
 *  4. Call scan-service  /api/scans/{id}/issues/bulk → persist issues
 *  5. Call scan-service  /api/scans/{id}/complete
 *  6. Return scanId + issue summary to caller
 */
@Service
public class UploadScanService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".java", ".py", ".js", ".ts", ".jsx", ".tsx", ".zip"
    );

    private static final Set<String> VENDOR_DIRS = Set.of(
            "node_modules", "__pycache__", ".venv", "venv", ".git",
            "target", "build", "dist", ".idea", ".gradle"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${analyzer.service.url}")
    private String analyzerUrl;

    @Value("${scan.service.url}")
    private String scanUrl;

    @Value("${upload.temp.dir}")
    private String tempBaseDir;

    public UploadScanService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // -----------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------

    public UploadScanResponse scan(
            List<MultipartFile> files,
            String language,
            Long projectId
    ) throws Exception {

        // 1. Save files to a temp workspace
        Path workspace = createWorkspace();
        try {
            saveFiles(files, workspace);

            // 2. Ask analyzer-service to analyse the workspace path
            List<Map<String, Object>> rawIssues = callAnalyzer(workspace, language);

            // 3. Persist in scan-service
            Long scanId = createScanRecord(projectId, language, workspace.getFileName().toString());
            if (!rawIssues.isEmpty()) {
                bulkStoreIssues(scanId, rawIssues);
            }
            completeScan(scanId);

            // 4. Build local response (rich – includes issues inline)
            return buildResponse(scanId, rawIssues);

        } finally {
            // Clean up temp workspace
            deleteWorkspace(workspace);
        }
    }

    // -----------------------------------------------------------------
    // Step helpers
    // -----------------------------------------------------------------

    private Path createWorkspace() throws IOException {
        Path base = Path.of(tempBaseDir);
        Files.createDirectories(base);
        return Files.createTempDirectory(base, "upload-");
    }

    private void saveFiles(List<MultipartFile> files, Path workspace) throws IOException {
        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) continue;

            // Normalise path separators from browser (some send full paths)
            String safeName = originalName.replace('\\', '/');
            String ext = extension(safeName);

            if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) continue;

            // Skip vendor directories anywhere in the path
            boolean inVendor = Arrays.stream(safeName.split("/"))
                    .anyMatch(VENDOR_DIRS::contains);
            if (inVendor) continue;

            if (ext.equalsIgnoreCase(".zip")) {
                extractZip(file.getInputStream(), workspace);
            } else {
                Path target = workspace.resolve(safeName).normalize();
                // Security: ensure target stays inside workspace
                if (!target.startsWith(workspace)) continue;
                Files.createDirectories(target.getParent());
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void extractZip(InputStream zipIn, Path workspace) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipIn)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) { zis.closeEntry(); continue; }

                String name = entry.getName().replace('\\', '/');
                boolean inVendor = Arrays.stream(name.split("/")).anyMatch(VENDOR_DIRS::contains);
                if (inVendor) { zis.closeEntry(); continue; }

                String ext = extension(name);
                if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase()) || ext.equalsIgnoreCase(".zip")) {
                    zis.closeEntry();
                    continue;
                }

                Path target = workspace.resolve(name).normalize();
                if (!target.startsWith(workspace)) { zis.closeEntry(); continue; }
                Files.createDirectories(target.getParent());
                Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                zis.closeEntry();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> callAnalyzer(Path workspace, String language) {
        String url = analyzerUrl + "/api/analyze/local";

        Map<String, String> body = Map.of(
                "path", workspace.toAbsolutePath().toString(),
                "language", language.toUpperCase()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Analyzer call failed: " + e.getMessage());
            throw new RuntimeException("Analysis failed: " + e.getMessage(), e);
        }
    }

    private Long createScanRecord(Long projectId, String language, String folderName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("projectId", projectId != null ? projectId : 1L);
        body.put("repositoryOwner", "local-upload");
        body.put("repositoryName", folderName != null ? folderName : "upload");
        body.put("language", language);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    scanUrl + "/api/scans", request, Map.class);
            Object id = response.getBody() != null ? response.getBody().get("id") : null;
            return id != null ? Long.parseLong(id.toString()) : null;
        } catch (Exception e) {
            System.err.println("Failed to create scan record: " + e.getMessage());
            throw new RuntimeException("Could not create scan record: " + e.getMessage(), e);
        }
    }

    private void bulkStoreIssues(Long scanId, List<Map<String, Object>> rawIssues) {
        // Remap CodeIssue field names → BulkIssueRequest field names
        List<Map<String, Object>> mapped = rawIssues.stream().map(m -> {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("severity",    str(m.get("severity")));
            b.put("type",        str(m.get("type")));
            b.put("message",     str(m.get("message")));
            b.put("filePath",    str(m.get("file")));           // file → filePath
            b.put("lineNumber",  m.get("line"));                // line → lineNumber
            b.put("ruleId",      str(m.get("rule")));           // rule → ruleId
            b.put("suggestion",  str(m.get("recommendation"))); // recommendation → suggestion
            return b;
        }).toList();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(mapped, headers);
        try {
            restTemplate.postForEntity(
                    scanUrl + "/api/scans/" + scanId + "/issues/bulk",
                    request, Void.class);
        } catch (Exception e) {
            System.err.println("Bulk issue store failed: " + e.getMessage());
        }
    }

    private void completeScan(Long scanId) {
        try {
            restTemplate.postForEntity(
                    scanUrl + "/api/scans/" + scanId + "/complete",
                    null, Void.class);
        } catch (Exception e) {
            System.err.println("Complete scan call failed: " + e.getMessage());
        }
    }

    private UploadScanResponse buildResponse(Long scanId, List<Map<String, Object>> rawIssues) {
        long critical = count(rawIssues, "CRITICAL");
        long high     = count(rawIssues, "HIGH");
        long medium   = count(rawIssues, "MEDIUM");
        long low      = count(rawIssues, "LOW");

        int penalty = (int)(critical * 10 + high * 6 + medium * 3 + low);
        int score   = Math.max(0, 100 - penalty);

        List<UploadScanResponse.IssueDto> issues = rawIssues.stream()
                .map(m -> {
                    UploadScanResponse.IssueDto d = new UploadScanResponse.IssueDto();
                    d.setSeverity(str(m.get("severity")));
                    d.setType(str(m.get("type")));
                    d.setMessage(str(m.get("message")));
                    d.setFilePath(str(m.get("file")));
                    Object ln = m.get("line");
                    d.setLineNumber(ln instanceof Number ? ((Number) ln).intValue() : 0);
                    d.setRuleId(str(m.get("rule")));
                    d.setSuggestion(str(m.get("recommendation")));
                    return d;
                })
                .toList();

        UploadScanResponse resp = new UploadScanResponse();
        resp.setScanId(scanId);
        resp.setStatus("COMPLETED");
        resp.setTotalIssues(rawIssues.size());
        resp.setCriticalIssues((int) critical);
        resp.setHighIssues((int) high);
        resp.setMediumIssues((int) medium);
        resp.setLowIssues((int) low);
        resp.setQualityScore(score);
        resp.setIssues(issues);
        return resp;
    }

    // -----------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : "";
    }

    private long count(List<Map<String, Object>> issues, String sev) {
        return issues.stream()
                .filter(m -> sev.equalsIgnoreCase(str(m.get("severity"))))
                .count();
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private void deleteWorkspace(Path workspace) {
        try {
            Files.walk(workspace)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("Workspace cleanup failed: " + e.getMessage());
        }
    }
}
