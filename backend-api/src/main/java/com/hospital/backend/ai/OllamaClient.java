package com.hospital.backend.ai;

import com.hospital.backend.ai.OllamaDtos.GenerateRequest;
import com.hospital.backend.ai.OllamaDtos.GenerateResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// Thin wrapper over the Ollama /api/generate HTTP call. Returns the raw model text;
// it does not know about prompts, parsing or caching — those live in the service.
@Component
public class OllamaClient {

    private final WebClient ollamaWebClient;
    private final OllamaProperties props;

    public OllamaClient(WebClient ollamaWebClient, OllamaProperties props) {
        this.ollamaWebClient = ollamaWebClient;
        this.props = props;
    }

    public String generate(String prompt) {
        try {
            GenerateResponse response = ollamaWebClient.post()
                    .uri("/api/generate")
                    .bodyValue(GenerateRequest.of(props.model(), prompt))
                    .retrieve()
                    .bodyToMono(GenerateResponse.class)
                    .block(props.timeout());

            return response == null ? null : response.response();
        } catch (RuntimeException e) {
            throw new AiAnalysisException("Language model request failed", e);
        }
    }
}
