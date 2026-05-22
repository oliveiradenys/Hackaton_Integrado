package com.fiap.report.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String status;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String contentText;

    @Column(columnDefinition = "TEXT")
    private String components;

    @Column(columnDefinition = "TEXT")
    private String risks;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getContentText() {
        return contentText;
    }

    public String getComponents() {
        return components;
    }

    public String getRisks() {
        return risks;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public void setRisks(String risks) {
        this.risks = risks;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
}
