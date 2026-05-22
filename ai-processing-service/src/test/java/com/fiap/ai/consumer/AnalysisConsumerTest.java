package com.fiap.ai.consumer;

import com.fiap.ai.client.OpenAiClient;
import com.fiap.ai.dto.AiAnalysisResponse;
import com.fiap.ai.entity.Analysis;
import com.fiap.ai.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AnalysisConsumerTest {

    @Test
    void deveProcessarAnaliseEAtualizarStatusParaDone() {
        AnalysisRepository repository = Mockito.mock(AnalysisRepository.class);
        OpenAiClient openAiClient = Mockito.mock(OpenAiClient.class);

        Analysis analysis = new Analysis();
        analysis.setId(1L);
        analysis.setFileName("diagrama.pdf");
        analysis.setStatus("PENDING");
        analysis.setContentText("API Gateway, RabbitMQ, PostgreSQL e microsserviços");

        when(repository.findById(1L)).thenReturn(Optional.of(analysis));
        when(openAiClient.analyzeArchitecture("diagrama.pdf", analysis.getContentText()))
                .thenReturn(new AiAnalysisResponse("Componentes", "Riscos", "Recomendações"));

        AnalysisConsumer consumer = new AnalysisConsumer(repository, openAiClient);

        consumer.process("1");

        verify(repository).findById(1L);
        verify(repository, times(2)).save(analysis);

        assertEquals("DONE", analysis.getStatus());
        assertEquals("Componentes", analysis.getComponents());
        assertEquals("Riscos", analysis.getRisks());
        assertEquals("Recomendações", analysis.getRecommendations());
    }
}
