package com.fiap.ai.dto;

public class AiAnalysisResponse {

    private String components;
    private String risks;
    private String recommendations;

    public AiAnalysisResponse() {
    }

    public AiAnalysisResponse(String components, String risks, String recommendations) {
        this.components = components;
        this.risks = risks;
        this.recommendations = recommendations;
    }

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getRisks() {
        return risks;
    }

    public void setRisks(String risks) {
        this.risks = risks;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
}