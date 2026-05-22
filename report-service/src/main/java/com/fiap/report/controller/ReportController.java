package com.fiap.report.controller;

import com.fiap.report.entity.Analysis;
import com.fiap.report.repository.AnalysisRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final AnalysisRepository repository;

    public ReportController(AnalysisRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Analysis> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Analysis findById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow();
    }
}