package com.fiap.upload.service;

import com.fiap.upload.config.RabbitMQConfig;
import com.fiap.upload.entity.Analysis;
import com.fiap.upload.repository.AnalysisRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalysisService {

    private final AnalysisRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public AnalysisService(AnalysisRepository repository,
                           RabbitTemplate rabbitTemplate) {

        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Analysis save(String fileName) {
        return createAnalysis(fileName, null);
    }

    public Analysis saveUploadedFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo obrigatório.");
        }

        String originalFileName = file.getOriginalFilename();
        String safeFileName = originalFileName == null || originalFileName.isBlank()
                ? "arquivo-sem-nome"
                : originalFileName;

        String contentText = extractText(file);
        return createAnalysis(safeFileName, contentText);
    }

    private Analysis createAnalysis(String fileName, String contentText) {
        Analysis analysis = new Analysis();
        analysis.setFileName(fileName);
        analysis.setContentText(contentText);
        analysis.setStatus("PENDING");
        analysis.setCreatedAt(LocalDateTime.now());

        Analysis saved = repository.save(analysis);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.QUEUE_NAME,
                saved.getId().toString()
        );

        return saved;
    }

    private String extractText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();

        if (fileName.endsWith(".pdf") || contentType.contains("pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                return new PDFTextStripper().getText(document);
            }
        }

        if (fileName.endsWith(".txt") || contentType.startsWith("text/")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        return "Arquivo recebido, mas este tipo ainda não possui extração de texto automática. Nome: " + fileName;
    }

    public List<Analysis> listAll() {
        return repository.findAll();
    }
}
