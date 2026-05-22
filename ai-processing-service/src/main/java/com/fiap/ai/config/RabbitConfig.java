package com.fiap.ai.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue analysisQueue() {
        return new Queue("analysis.queue", true);
    }
}