package com.fiap.upload.controller;

import com.fiap.upload.entity.Analysis;
import com.fiap.upload.service.AnalysisService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/analysis")
public class UploadController {

    private final AnalysisService service;

    public UploadController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping
    public Analysis create(@RequestParam String fileName) {
        return service.save(fileName);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Analysis upload(@RequestParam("file") MultipartFile file) throws IOException {
        return service.saveUploadedFile(file);
    }

    @GetMapping
    public List<Analysis> list() {
        return service.listAll();
    }
}
