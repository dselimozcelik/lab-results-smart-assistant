package com.hospital.backend.ai;

// Thrown when the LLM call fails (timeout, Ollama down, unparseable response).
// Translated to a graceful HTTP error by the global exception handler.
public class AiAnalysisException extends RuntimeException {

    public AiAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
