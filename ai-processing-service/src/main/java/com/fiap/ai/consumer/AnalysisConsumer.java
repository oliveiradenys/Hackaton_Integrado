package com.fiap.ai.consumer;

import com.fiap.ai.client.OpenAiClient;
import com.fiap.ai.dto.AiAnalysisResponse;
import com.fiap.ai.entity.Analysis;
import com.fiap.ai.repository.AnalysisRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisConsumer {

    private final AnalysisRepository repository;
    private final OpenAiClient openAiClient;

    public AnalysisConsumer(AnalysisRepository repository, OpenAiClient openAiClient) {
        this.repository = repository;
        this.openAiClient = openAiClient;
    }

    @RabbitListener(queues = "analysis.queue")
    public void process(String message) {

        Analysis analysis = findAnalysis(message);

        try {
            analysis.setStatus("PROCESSING");
            repository.save(analysis);

            System.out.println("=================================");
            System.out.println("Processando análise ID/mensagem: " + message);
            System.out.println("Arquivo: " + analysis.getFileName());
            System.out.println("=================================");

            AiAnalysisResponse aiResponse = openAiClient.analyzeArchitecture(
                    analysis.getFileName(),
                    analysis.getContentText()
            );

            analysis.setComponents(aiResponse.getComponents());
            analysis.setRisks(aiResponse.getRisks());
            analysis.setRecommendations(aiResponse.getRecommendations());
            analysis.setStatus("DONE");

            repository.save(analysis);

            System.out.println("=================================");
            System.out.println("Processamento finalizado!");
            System.out.println("Relatório gerado com sucesso!");
            System.out.println("=================================");

        } catch (Exception e) {
            analysis.setStatus("ERROR");
            analysis.setRisks("Erro ao processar análise: " + e.getMessage());
            analysis.setRecommendations("Verificar logs do ai-processing-service, conexão com RabbitMQ/PostgreSQL e configuração da OPENAI_API_KEY.");

            repository.save(analysis);

            System.out.println("=================================");
            System.out.println("Erro ao processar mensagem: " + message);
            System.out.println(e.getMessage());
            System.out.println("=================================");
        }
    }

    private Analysis findAnalysis(String message) {
        try {
            Long id = Long.parseLong(message);
            return repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Análise não encontrada para o ID: " + id));
        } catch (NumberFormatException ignored) {
            return repository.findFirstByFileNameOrderByIdDesc(message)
                    .orElseThrow(() -> new RuntimeException("Análise não encontrada para o arquivo: " + message));
        }
    }
}
