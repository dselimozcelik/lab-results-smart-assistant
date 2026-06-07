package com.hospital.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.backend.audit.PollingAuditLogRepository;
import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.LabResultIngestionService;
import com.hospital.backend.labresult.LabResultPoller;
import com.hospital.backend.labresult.Sample;
import com.hospital.backend.labresult.SampleBatchDto;
import com.hospital.backend.labresult.SampleRepository;
import com.hospital.backend.labresult.TestResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
@Testcontainers
class BackendApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LabResultIngestionService ingestionService;

    @Autowired
    SampleRepository sampleRepository;

    @Autowired
    PollingAuditLogRepository auditRepository;

    @MockBean
    LabResultPoller poller;

    @BeforeEach
    void cleanDatabase() {
        auditRepository.deleteAll();
        sampleRepository.deleteAll();
    }

    @Test
    void loginProtectsEndpointsAndValidatesQueryParameters() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"doctor","password":"Doctor123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DOCTOR"))
                .andReturn().getResponse().getContentAsString();
        JsonNode login = objectMapper.readTree(loginJson);
        String bearer = "Bearer " + login.get("token").asText();

        mockMvc.perform(get("/api/patients").header("Authorization", bearer))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/patients?status=NOPE").header("Authorization", bearer))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/patients?from=not-a-date").header("Authorization", bearer))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/patients?from=2026-06-08T12:00:00Z&to=2026-06-08T11:00:00Z")
                        .header("Authorization", bearer))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/patients?size=999999").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void openApiDocumentationIsPubliclyAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isString());
    }

    @Test
    void invalidTestIsStoredAndExplainedInAuditDetails() {
        SampleBatchDto tube = new SampleBatchDto(
                "S-INTEGRATION", "P-INTEGRATION", Instant.now(), "DEV-1",
                List.of(new TestResultDto("GLU", "Glucose", "mg/dL", null, 70.0, 110.0)));

        ingestionService.ingest(List.of(tube));

        Sample stored = sampleRepository.findByPatientIdWithTests("P-INTEGRATION").get(0);
        assertThat(stored.getTests()).singleElement()
                .extracting(test -> test.getAnomalyStatus())
                .isEqualTo(AnomalyStatus.INVALID);
        assertThat(auditRepository.findAll()).singleElement()
                .extracting(log -> log.getDetails())
                .asString()
                .contains("Invalid test S-INTEGRATION/GLU")
                .contains("\"outcome\":\"PROCESSED\"");
    }
}
