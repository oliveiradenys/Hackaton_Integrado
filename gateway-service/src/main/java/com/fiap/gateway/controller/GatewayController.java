package com.fiap.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
public class GatewayController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.upload.url}")
    private String uploadServiceUrl;

    @Value("${services.report.url}")
    private String reportServiceUrl;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("gateway-service UP");
    }

    @PostMapping("/analysis")
    public ResponseEntity<String> createAnalysis(@RequestParam String fileName) {

        String url = UriComponentsBuilder
                .fromHttpUrl(uploadServiceUrl + "/analysis")
                .queryParam("fileName", fileName)
                .toUriString();

        String response = restTemplate.postForObject(
                url,
                null,
                String.class
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/analysis/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAnalysis(@RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadServiceUrl + "/analysis/upload";

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                String originalFilename = file.getOriginalFilename();
                return originalFilename == null || originalFilename.isBlank() ? "arquivo" : originalFilename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                requestEntity,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/analysis")
    public ResponseEntity<String> findAllAnalysis() {
        String response = restTemplate.getForObject(
                uploadServiceUrl + "/analysis",
                String.class
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports")
    public ResponseEntity<String> findAllReports() {

        String response = restTemplate.getForObject(
                reportServiceUrl + "/reports",
                String.class
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<String> findReportById(@PathVariable Long id) {

        String response = restTemplate.getForObject(
                reportServiceUrl + "/reports/" + id,
                String.class
        );

        return ResponseEntity.ok(response);
    }
}
