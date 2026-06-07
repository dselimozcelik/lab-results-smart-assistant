package com.hospital.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lab-results/{id}/ai-analysis")
public class AiAnalysisController {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final AiAnalysisService service;
    private final ObjectMapper objectMapper;

    public AiAnalysisController(AiAnalysisService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    // Synchronous for the demo (README notes production would queue this).
    @PostMapping
    public AiAnalysisResponse analyze(@PathVariable Long id) {
        AiAnalysis a = service.analyze(id);
        return new AiAnalysisResponse(
                a.getId(), a.getLabResultId(), a.getModel(), a.getPromptVersion(),
                a.getSummary(),
                readList(a.getFlaggedTests()),
                readList(a.getSuggestedFollowups()),
                a.getDisclaimer(), a.getCreatedAt());
    }

    private List<String> readList(String json) {
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (JsonProcessingException e) {
            throw new AiAnalysisException("Stored analysis has malformed list data", e);
        }
    }
}
