package com.hospital.backend.patient;

import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.LabResult;
import com.hospital.backend.labresult.Sample;
import com.hospital.backend.labresult.SampleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Exercises PatientService against a REAL Postgres: the detail dedup and the history ordering/limit
// depend on the JOIN FETCH and the SQL ordering, so an in-memory mock would not prove them.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PatientServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    SampleRepository sampleRepository;

    private PatientService service;

    @BeforeEach
    void setUp() {
        service = new PatientService(sampleRepository);
    }

    private Sample tube(String sampleId, String patientId, Instant measuredAt) {
        return new Sample(sampleId, patientId, measuredAt, "DEV-1");
    }

    @Test
    void getPatientReturnsEachTubeOnceEvenWithAMultiTestPanel() {
        Sample s = tube("S-DEDUP", "P-DEDUP", Instant.now());
        s.addTest(new LabResult("GLU", "Glucose", 95.0, "mg/dL", 70.0, 110.0, AnomalyStatus.NORMAL));
        s.addTest(new LabResult("HGB", "Haemoglobin", 10.0, "g/dL", 12.0, 16.0, AnomalyStatus.LOW));
        s.addTest(new LabResult("NA", "Sodium", 140.0, "mmol/L", 135.0, 145.0, AnomalyStatus.NORMAL));
        sampleRepository.saveAndFlush(s);

        PatientDetailResponse detail = service.getPatient("P-DEDUP");

        // JOIN FETCH repeats the tube once per test row; the service must collapse it back to one.
        assertThat(detail.samples()).hasSize(1);
        assertThat(detail.samples().get(0).tests()).hasSize(3);
    }

    @Test
    void getPatientThrowsWhenNoTubesExist() {
        assertThatThrownBy(() -> service.getPatient("P-MISSING"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTestHistoryReturnsNumericValuesOldestToNewest() {
        Instant t0 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        savePoint("S-H1", "P-H", "GLU", 90.0, t0.minus(2, ChronoUnit.DAYS));
        savePoint("S-H2", "P-H", "GLU", 120.0, t0.minus(1, ChronoUnit.DAYS));
        savePoint("S-H3", "P-H", "GLU", 150.0, t0);

        List<TestHistoryPoint> history = service.getTestHistory("P-H", "GLU");

        assertThat(history).extracting(TestHistoryPoint::value)
                .containsExactly(90.0, 120.0, 150.0); // chronological, not the DESC query order
    }

    @Test
    void getTestHistorySkipsRowsWithoutAValue() {
        Instant t0 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        savePoint("S-N1", "P-N", "NA", 140.0, t0.minus(1, ChronoUnit.DAYS));
        // An INVALID reading with no value must not appear as a (null) point in the trend.
        Sample invalid = tube("S-N2", "P-N", t0);
        invalid.addTest(new LabResult("NA", "Sodium", null, "mmol/L", 135.0, 145.0, AnomalyStatus.INVALID));
        sampleRepository.saveAndFlush(invalid);

        List<TestHistoryPoint> history = service.getTestHistory("P-N", "NA");

        assertThat(history).extracting(TestHistoryPoint::value).containsExactly(140.0);
    }

    private void savePoint(String sampleId, String patientId, String testCode, double value, Instant at) {
        Sample s = tube(sampleId, patientId, at);
        s.addTest(new LabResult(testCode, "Test", value, "u", 0.0, 200.0, AnomalyStatus.NORMAL));
        sampleRepository.saveAndFlush(s);
    }
}
