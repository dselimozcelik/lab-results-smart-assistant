package com.hospital.backend.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// Externalised Ollama config. Model/URL/timeout come from application.yml, not code.
@ConfigurationProperties(prefix = "lab.ollama")
public record OllamaProperties(
        String baseUrl,
        String model,
        Duration timeout
) {
}
