package com.hospital.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.LabResult;
import com.hospital.backend.labresult.Sample;
import com.hospital.backend.labresult.SampleRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests the AI service WITHOUT a real Ollama: MockWebServer stands in for the LLM, repos are
// mocked. Verifies the panel summary covers all tests, the disclaimer is backend-enforced, and a
// cache hit skips the LLM call.
class AiAnalysisServiceTest {

    private MockWebServer ollamaServer;
    private SampleRepository sampleRepository;
    private AiAnalysisRepository aiAnalysisRepository;
    private AiAnalysisService service;

    @BeforeEach
    void setUp() throws Exception {
        ollamaServer = new MockWebServer();
        ollamaServer.start();

        sampleRepository = mock(SampleRepository.class);
        aiAnalysisRepository = mock(AiAnalysisRepository.class);

        OllamaProperties props = new OllamaProperties(
                ollamaServer.url("/").toString(), "test-model", Duration.ofSeconds(5));
        WebClient webClient = WebClient.builder().baseUrl(props.baseUrl()).build();
        OllamaClient ollamaClient = new OllamaClient(webClient, props);

        service = new AiAnalysisService(sampleRepository, aiAnalysisRepository,
                new AnomalySummaryBuilder(), new PromptTemplate(), ollamaClient, props,
                new ObjectMapper());

        // Default: the analysis cache is empty, and save() returns its argument.
        when(aiAnalysisRepository.findBySampleFkAndModelAndPromptVersion(anyLong(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(aiAnalysisRepository.save(any(AiAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        ollamaServer.shutdown();
    }

    private Sample sampleWithPanel() {
        Sample s = new Sample("S-100", "P-1", Instant.now(), "DEV-1");
        s.addTest(new LabResult("GLU", "Glucose", 95.0, "mg/dL", 70.0, 110.0, AnomalyStatus.NORMAL));
        s.addTest(new LabResult("WBC", "White Blood Cell Count", 14.0, "10^9/L", 4.0, 11.0, AnomalyStatus.HIGH));
        s.addTest(new LabResult("K", "Potassium", null, "mmol/L", 3.5, 5.1, AnomalyStatus.INVALID));
        return s;
    }

    private void enqueueModelJson() throws Exception {
        // Ollama envelope: "response" is a STRING that itself contains the model's JSON.
        String modelJson = "{\"summary\":\"Panel reviewed.\","
                + "\"flaggedTests\":[\"White Blood Cell Count\"],"
                + "\"suggestedFollowups\":[\"Repeat the potassium draw.\"]}";
        ObjectMapper mapper = new ObjectMapper();
        String envelope = "{\"response\":" + mapper.writeValueAsString(modelJson) + "}";
        ollamaServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(envelope));
    }

    @Test
    void buildsPanelPromptOverAllTestsAndEnforcesDisclaimer() throws Exception {
        when(sampleRepository.findBySampleId("S-100")).thenReturn(Optional.of(sampleWithPanel()));
        enqueueModelJson();

        AiAnalysis result = service.analyze("S-100");

        // The prompt sent to the LLM mentions every test in the panel, including the INVALID one.
        RecordedRequest request = ollamaServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).contains("Glucose").contains("White Blood Cell Count").contains("Potassium");
        assertThat(body).contains("INVALID");
        // The hard rule (Turkish: "do not invent reference ranges") must be present in the prompt.
        assertThat(body).contains("UYDURMA");

        // The disclaimer is set by the backend, not taken from the model output.
        assertThat(result.getDisclaimer()).contains("not a diagnosis");
        assertThat(result.getPromptVersion()).isEqualTo("v4");
        verify(aiAnalysisRepository).save(any(AiAnalysis.class));
    }

    @Test
    void cacheHitSkipsTheLlmCall() {
        Sample s = sampleWithPanel();
        when(sampleRepository.findBySampleId("S-100")).thenReturn(Optional.of(s));
        AiAnalysis cached = new AiAnalysis(1L, "test-model", "v4", "cached", "[]", "[]", "disc");
        when(aiAnalysisRepository.findBySampleFkAndModelAndPromptVersion(any(), anyString(), anyString()))
                .thenReturn(Optional.of(cached));

        AiAnalysis result = service.analyze("S-100");

        assertThat(result).isSameAs(cached);
        assertThat(ollamaServer.getRequestCount()).isZero(); // no LLM call
        verify(aiAnalysisRepository, never()).save(any());
    }

    @Test
    void flaggedTestsComeFromBackendStatusesNotModelClaims() throws Exception {
        when(sampleRepository.findBySampleId("S-100")).thenReturn(Optional.of(sampleWithPanel()));
        String modelJson = "{\"summary\":\"Panel reviewed.\","
                + "\"flaggedTests\":[\"Invented Test\"],"
                + "\"suggestedFollowups\":[\"  Repeat panel.  \",\"Repeat panel.\",\"\"]}";
        String envelope = "{\"response\":" + new ObjectMapper().writeValueAsString(modelJson) + "}";
        ollamaServer.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody(envelope));

        AiAnalysis result = service.analyze("S-100");

        assertThat(result.getFlaggedTests())
                .contains("White Blood Cell Count")
                .contains("Potassium")
                .doesNotContain("Invented Test");
        assertThat(result.getSuggestedFollowups()).isEqualTo("[\"Repeat panel.\"]");
    }

    @Test
    void emptySummaryIsRejected() throws Exception {
        when(sampleRepository.findBySampleId("S-100")).thenReturn(Optional.of(sampleWithPanel()));
        String modelJson = "{\"summary\":\" \",\"flaggedTests\":[],\"suggestedFollowups\":[]}";
        String envelope = "{\"response\":" + new ObjectMapper().writeValueAsString(modelJson) + "}";
        ollamaServer.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody(envelope));

        assertThatThrownBy(() -> service.analyze("S-100"))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("empty summary");
        verify(aiAnalysisRepository, never()).save(any());
    }

    @Test
    void malformedModelJsonIsRejected() throws Exception {
        when(sampleRepository.findBySampleId("S-100")).thenReturn(Optional.of(sampleWithPanel()));
        String envelope = "{\"response\":" + new ObjectMapper().writeValueAsString("not-json") + "}";
        ollamaServer.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody(envelope));

        assertThatThrownBy(() -> service.analyze("S-100"))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("malformed");
    }
}
