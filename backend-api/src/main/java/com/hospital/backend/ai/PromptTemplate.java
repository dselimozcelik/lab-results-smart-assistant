package com.hospital.backend.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

// Loads the prompt template file once at startup and fills in the result summary.
// The version string is persisted with each analysis so a prompt change invalidates the cache.
@Component
public class PromptTemplate {

    public static final String VERSION = "v3";

    private final String template;

    public PromptTemplate() {
        try {
            this.template = StreamUtils.copyToString(
                    new ClassPathResource("prompts/ai-analysis-" + VERSION + ".txt").getInputStream(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load prompt template", e);
        }
    }

    public String render(String resultSummary) {
        return template.formatted(resultSummary);
    }
}
