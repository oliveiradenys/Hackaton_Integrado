package com.fiap.upload.service;

import com.fiap.upload.entity.Analysis;
import com.fiap.upload.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnalysisServiceTest {

    @Test
    void shouldSaveAnalysisAndSendMessageToQueue() {
        AnalysisRepository repository = Mockito.mock(AnalysisRepository.class);
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);

        Analysis savedAnalysis = new Analysis();
        savedAnalysis.setId(1L);
        savedAnalysis.setFileName("teste.pdf");
        savedAnalysis.setStatus("PENDING");

        when(repository.save(any(Analysis.class))).thenReturn(savedAnalysis);

        AnalysisService service = new AnalysisService(repository, rabbitTemplate);

        Analysis result = service.save("teste.pdf");

        assertEquals("teste.pdf", result.getFileName());
        assertEquals("PENDING", result.getStatus());

        verify(repository, times(1)).save(any(Analysis.class));
        verify(rabbitTemplate, times(1)).convertAndSend("analysis.queue", "1");
    }
}
