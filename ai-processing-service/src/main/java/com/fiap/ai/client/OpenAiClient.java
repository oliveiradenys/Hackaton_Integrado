package com.fiap.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.ai.dto.AiAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class OpenAiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    public OpenAiClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public AiAnalysisResponse analyzeArchitecture(String fileName, String contentText) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackAnalysis(fileName, contentText, "OPENAI_API_KEY não configurada. Resultado demo gerado localmente.");
        }

        try {
            String prompt = buildPrompt(fileName, contentText);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "input", prompt
            );

            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseOpenAiResponse(response);

        } catch (Exception e) {
            return fallbackAnalysis(fileName, contentText, "Falha ao consultar ou interpretar a resposta da OpenAI: " + e.getMessage());
        }
    }

    private String buildPrompt(String fileName, String contentText) {
        String safeContent = contentText == null || contentText.isBlank()
                ? "Nenhum texto foi extraído do arquivo. Analise com base no nome e no contexto do sistema."
                : contentText;

        if (safeContent.length() > 12000) {
            safeContent = safeContent.substring(0, 12000);
        }

        return """
                Você é um arquiteto de software sênior.

                Analise um arquivo de arquitetura de software enviado para um sistema de auditoria técnica.

                Nome do arquivo recebido: %s

                Conteúdo extraído do arquivo:
                %s

                Retorne somente um JSON válido, sem markdown, sem texto antes ou depois, exatamente neste formato:

                {
                  "components": "liste os componentes arquiteturais identificados",
                  "risks": "liste possíveis riscos arquiteturais",
                  "recommendations": "liste recomendações técnicas"
                }

                Regras obrigatórias:
                - Seja objetivo.
                - Não use markdown.
                - Não retorne explicações fora do JSON.
                - Foque em API Gateway, microsserviços, banco de dados, mensageria, escalabilidade, segurança e observabilidade.
                - Não invente detalhes específicos que não possam ser inferidos do contexto.
                """.formatted(fileName, safeContent);
    }

    private AiAnalysisResponse parseOpenAiResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);

        String text = root.path("output_text").asText();

        if (text == null || text.isBlank()) {
            JsonNode output = root.path("output");

            if (output.isArray() && !output.isEmpty()) {
                JsonNode content = output.get(0).path("content");

                if (content.isArray() && !content.isEmpty()) {
                    text = content.get(0).path("text").asText();
                }
            }
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Resposta da OpenAI sem campo de texto útil.");
        }

        String cleanJson = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        return objectMapper.readValue(cleanJson, AiAnalysisResponse.class);
    }

    private AiAnalysisResponse fallbackAnalysis(String fileName, String contentText, String reason) {
        String context = contentText == null || contentText.isBlank()
                ? "O arquivo não teve texto extraído; análise feita pelo fluxo esperado de microsserviços."
                : "Texto extraído recebido com sucesso; análise local simplificada aplicada.";

        return new AiAnalysisResponse(
                "Arquivo: " + fileName + ". Componentes prováveis: API Gateway, upload-service, fila RabbitMQ, serviço de processamento, PostgreSQL e serviço de relatórios. " + context,
                "Riscos: dependência de serviços externos, ausência de autenticação, falta de validação profunda do arquivo, necessidade de observabilidade e possível ponto único de falha no banco/mensageria. Motivo do fallback: " + reason,
                "Recomendações: configurar OPENAI_API_KEY para análise real, adicionar autenticação/JWT, validar tipo/tamanho do arquivo, criar logs centralizados, healthchecks, retries na fila e métricas para acompanhamento do processamento."
        );
    }
}
