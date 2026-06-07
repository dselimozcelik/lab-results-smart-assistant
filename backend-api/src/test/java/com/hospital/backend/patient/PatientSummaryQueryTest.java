package com.hospital.backend.patient;

import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.LabResult;
import com.hospital.backend.labresult.Sample;
import com.hospital.backend.labresult.SampleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies the Level-1 aggregation query against a REAL Postgres (Testcontainers), since the
// CASE-based severity ranking and GROUP BY are database-specific. Flyway builds the schema.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PatientSummaryQueryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // The schema is owned by Flyway, not Hibernate; let Flyway run and validate against it.
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    SampleRepository sampleRepository;

    private Sample tube(String sampleId, String patientId, Instant measuredAt) {
        return new Sample(sampleId, patientId, measuredAt, "DEV-1");
    }

    private void addTest(Sample s, AnomalyStatus status) {
        s.addTest(new LabResult("GLU-" + status, "Glucose", 95.0, "mg/dL", 70.0, 110.0, status));
    }

    @Test
    void summarisesPerPatientWithWorstStatusAndCounts() {
        Instant now = Instant.now();

        // Patient A: two tubes, worst is CRITICAL, 3 tests total.
        Sample a1 = tube("S-A1", "P-A", now.minus(2, ChronoUnit.HOURS));
        addTest(a1, AnomalyStatus.NORMAL);
        addTest(a1, AnomalyStatus.HIGH);
        Sample a2 = tube("S-A2", "P-A", now);
        addTest(a2, AnomalyStatus.CRITICAL);

        // Patient B: one tube, only NORMAL.
        Sample b1 = tube("S-B1", "P-B", now.minus(1, ChronoUnit.HOURS));
        addTest(b1, AnomalyStatus.NORMAL);

        sampleRepository.saveAll(java.util.List.of(a1, a2, b1));
        sampleRepository.flush();

        Page<PatientSummaryRow> page = summaries(null, null, null, null, null);

        assertThat(page.getTotalElements()).isEqualTo(2);

        PatientSummaryRow a = page.getContent().stream()
                .filter(r -> r.getPatientId().equals("P-A")).findFirst().orElseThrow();
        assertThat(a.getTestCount()).isEqualTo(3);
        assertThat(a.getSampleCount()).isEqualTo(2);
        assertThat(a.getWorstSeverity()).isEqualTo(4); // CRITICAL

        PatientSummaryRow b = page.getContent().stream()
                .filter(r -> r.getPatientId().equals("P-B")).findFirst().orElseThrow();
        assertThat(b.getTestCount()).isEqualTo(1);
        assertThat(b.getWorstSeverity()).isEqualTo(1); // NORMAL
    }

    @Test
    void filtersPatientsAndCalculatesSummaryFromMatchingTests() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Sample a = tube("S-FILTER-A", "P-FILTER-A", now);
        a.addTest(new LabResult("GLU", "Glucose", 150.0, "mg/dL", 70.0, 110.0, AnomalyStatus.HIGH));
        a.addTest(new LabResult("HGB", "Haemoglobin", 10.0, "g/dL", 12.0, 16.0, AnomalyStatus.LOW));

        Sample b = tube("S-FILTER-B", "P-FILTER-B", now.minus(10, ChronoUnit.DAYS));
        b.addTest(new LabResult("GLU", "Glucose", 95.0, "mg/dL", 70.0, 110.0, AnomalyStatus.NORMAL));

        sampleRepository.saveAll(java.util.List.of(a, b));
        sampleRepository.flush();

        Page<PatientSummaryRow> lowHgb = summaries(
                "p-filter", "hgb", AnomalyStatus.LOW,
                now.minus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS));

        assertThat(lowHgb.getTotalElements()).isEqualTo(1);
        PatientSummaryRow result = lowHgb.getContent().get(0);
        assertThat(result.getPatientId()).isEqualTo("P-FILTER-A");
        assertThat(result.getTestCount()).isEqualTo(1);
        assertThat(result.getSampleCount()).isEqualTo(1);
        assertThat(result.getWorstSeverity()).isEqualTo(3);
        assertThat(result.getHighCount()).isZero();
    }

    private Page<PatientSummaryRow> summaries(
            String patientId, String testCode, AnomalyStatus status, Instant from, Instant to) {
        return sampleRepository.findPatientSummaries(
                patientId, testCode, status, from, to, PageRequest.of(0, 20));
    }
}
