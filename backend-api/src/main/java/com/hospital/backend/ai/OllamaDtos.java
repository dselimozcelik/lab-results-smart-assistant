package com.hospital.backend.ai;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

// Small DTOs for the Ollama /api/generate call and for the model's JSON content.
class OllamaDtos {

    private OllamaDtos() {
    }

    // Request body sent to Ollama. stream=false (one response), format=json (force JSON),
    // options carries temperature=0 for deterministic output.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record GenerateRequest(String model, String prompt, boolean stream, String format,
                           Map<String, Object> options) {
        static GenerateRequest of(String model, String prompt) {
            return new GenerateRequest(model, prompt, false, "json", Map.of("temperature", 0));
        }
    }

    // Ollama's envelope. We only need the "response" field: the model's generated text.
    record GenerateResponse(String response) {
    }

    // The shape we ask the model to produce (no disclaimer: the backend enforces that).
    record ModelContent(String summary, List<String> flaggedTests, List<String> suggestedFollowups) {
    }
}
