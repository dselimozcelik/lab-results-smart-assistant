package com.hospital.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.backend.ai.OllamaDtos.ModelContent;
import com.hospital.backend.labresult.Sample;
import com.hospital.backend.labresult.SampleRepository;
import com.hospital.backend.labresult.AnomalyStatus;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Orchestrates one AI analysis for a tube (panel): cache lookup -> deterministic summary ->
// prompt -> Ollama -> parse JSON -> attach the backend-enforced disclaimer -> persist. Cached by
// (sampleFk, model, promptVersion) so a repeat request never re-calls the LLM.
@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    // The disclaimer is enforced by the backend, NOT produced by the model: a doctor must always
    // see this exact wording, so we never let a non-deterministic model phrase (or drop) it.
    private static final String DISCLAIMER =
            "This is a preliminary, AI-assisted analysis to support a doctor. "
            + "It is not a diagnosis and must be reviewed by a qualified clinician.";

    private final SampleRepository sampleRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final AnomalySummaryBuilder summaryBuilder;
    private final PromptTemplate promptTemplate;
    private final OllamaClient ollamaClient;
    private final OllamaProperties props;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(SampleRepository sampleRepository,
                             AiAnalysisRepository aiAnalysisRepository,
                             AnomalySummaryBuilder summaryBuilder,
                             PromptTemplate promptTemplate,
                             OllamaClient ollamaClient,
                             OllamaProperties props,
                             ObjectMapper objectMapper) {
        this.sampleRepository = sampleRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.summaryBuilder = summaryBuilder;
        this.promptTemplate = promptTemplate;
        this.ollamaClient = ollamaClient;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    // readOnly: this method only reads the tube and its lazy test collection (and writes the new
    // analysis through the repository's own transaction); declaring it documents intent and keeps
    // the lazy-load session open while the summary is built.
    @Transactional(readOnly = true)
    public AiAnalysis analyze(String sampleId) {
        Sample sample = sampleRepository.findBySampleId(sampleId)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found: " + sampleId));

        // Cache hit: same tube + model + prompt version already analysed.
        return aiAnalysisRepository
                .findBySampleFkAndModelAndPromptVersion(sample.getId(), props.model(), PromptTemplate.VERSION)
                .orElseGet(() -> generateAndSave(sample));
    }

    private AiAnalysis generateAndSave(Sample sample) {
        String prompt = promptTemplate.render(summaryBuilder.build(sample));

        String rawJson = ollamaClient.generate(prompt);
        if (rawJson == null || rawJson.isBlank()) {
            throw new AiAnalysisException("Empty response from the language model", null);
        }

        ModelContent content = validate(parse(rawJson));
        List<String> flaggedTests = sample.getTests().stream()
                .filter(test -> test.getAnomalyStatus() != AnomalyStatus.NORMAL)
                .map(test -> test.getTestName())
                .toList();

        AiAnalysis analysis = new AiAnalysis(
                sample.getId(), props.model(), PromptTemplate.VERSION,
                content.summary(),
                writeJson(flaggedTests),
                writeJson(content.suggestedFollowups()),
                DISCLAIMER);
        return aiAnalysisRepository.save(analysis);
    }

    private ModelContent parse(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, ModelContent.class);
        } catch (JsonProcessingException e) {
            // Truncate: model output can be long and may echo patient values; we only need a hint.
            log.warn("Could not parse model output as JSON: {}", truncate(rawJson));
            throw new AiAnalysisException("Language model returned malformed output", e);
        }
    }

    private String truncate(String value) {
        return value.length() <= 300 ? value : value.substring(0, 300) + "…";
    }

    private ModelContent validate(ModelContent content) {
        if (content.summary() == null || content.summary().isBlank()) {
            throw new AiAnalysisException("Language model returned an empty summary", null);
        }
        return new ModelContent(
                content.summary().trim(),
                List.of(),
                cleanList(content.suggestedFollowups()));
    }

    private List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String writeJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list == null ? List.of() : list);
        } catch (JsonProcessingException e) {
            throw new AiAnalysisException("Could not serialise model list field", e);
        }
    }
}
