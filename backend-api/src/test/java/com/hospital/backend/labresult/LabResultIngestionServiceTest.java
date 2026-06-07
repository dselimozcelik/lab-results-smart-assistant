package com.hospital.backend.labresult;

import com.hospital.backend.audit.PollingAuditLog;
import com.hospital.backend.audit.PollingAuditLogRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LabResultIngestionServiceTest {

    private SampleRepository sampleRepository;
    private PollingAuditLogRepository auditLogRepository;
    private ValidatorFactory validatorFactory;
    private LabResultIngestionService service;

    @BeforeEach
    void setUp() {
        sampleRepository = mock(SampleRepository.class);
        auditLogRepository = mock(PollingAuditLogRepository.class);
        validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator beanValidator = validatorFactory.getValidator();
        AnomalyClassifier classifier = new AnomalyClassifier(new AnomalyProperties(0.5));
        service = new LabResultIngestionService(sampleRepository, new SampleValidator(),
                new TestResultValidator(), beanValidator, classifier, auditLogRepository);
        // No tube has been seen before unless a test says otherwise.
        when(sampleRepository.existsBySampleId(anyString())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    private TestResultDto glucose(double value) {
        return new TestResultDto("GLU", "Glucose", "mg/dL", value, 70.0, 110.0);
    }

    private SampleBatchDto tube(String sampleId, Instant measuredAt, TestResultDto... tests) {
        return new SampleBatchDto(sampleId, "P-1", measuredAt, "DEV-1", List.of(tests));
    }

    @Test
    void validTubeIsStoredWithClassifiedTests() {
        service.ingest(List.of(tube("S-1", Instant.now(), glucose(95.0), glucose2())));

        ArgumentCaptor<Sample> saved = ArgumentCaptor.forClass(Sample.class);
        verify(sampleRepository).save(saved.capture());
        assertThat(saved.getValue().getTests()).hasSize(2);
        assertAuditCounts(1, 2, 0, 0);
    }

    private TestResultDto glucose2() {
        return new TestResultDto("K", "Potassium", "mmol/L", 4.2, 3.5, 5.1);
    }

    @Test
    void brokenTestIsStoredAsInvalidNotDropped() {
        TestResultDto noValue = new TestResultDto("GLU", "Glucose", "mg/dL", null, 70.0, 110.0);
        service.ingest(List.of(tube("S-2", Instant.now(), noValue, glucose2())));

        ArgumentCaptor<Sample> saved = ArgumentCaptor.forClass(Sample.class);
        verify(sampleRepository).save(saved.capture());
        List<LabResult> tests = saved.getValue().getTests();
        assertThat(tests).hasSize(2); // the broken one is kept
        assertThat(tests).anyMatch(t -> t.getAnomalyStatus() == AnomalyStatus.INVALID);
        assertAuditCounts(1, 1, 1, 0);
    }

    @Test
    void duplicateTubeIsLoggedNotInserted() {
        when(sampleRepository.existsBySampleId("S-3")).thenReturn(true);
        service.ingest(List.of(tube("S-3", Instant.now(), glucose(95.0))));

        verify(sampleRepository, never()).save(any());
        assertAuditCounts(1, 0, 0, 1);
    }

    @Test
    void staleTubeIsRejectedWhole() {
        Instant stale = Instant.now().minus(400, ChronoUnit.DAYS);
        service.ingest(List.of(tube("S-4", stale, glucose(95.0), glucose2())));

        verify(sampleRepository, never()).save(any());
        assertAuditCounts(1, 0, 2, 0); // both tests counted invalid
    }

    @Test
    void repeatedTestCodeInTubeKeepsFirstAndCountsSecondInvalid() {
        service.ingest(List.of(tube("S-5", Instant.now(), glucose(95.0), glucose(96.0))));

        ArgumentCaptor<Sample> saved = ArgumentCaptor.forClass(Sample.class);
        verify(sampleRepository).save(saved.capture());
        assertThat(saved.getValue().getTests()).hasSize(1);
        assertAuditCounts(1, 1, 1, 0);
    }

    private void assertAuditCounts(int fetched, int valid, int invalid, int duplicate) {
        ArgumentCaptor<PollingAuditLog> audit = ArgumentCaptor.forClass(PollingAuditLog.class);
        verify(auditLogRepository).save(audit.capture());
        PollingAuditLog log = audit.getValue();
        assertThat(log.getFetchedCount()).isEqualTo(fetched);
        assertThat(log.getValidCount()).isEqualTo(valid);
        assertThat(log.getInvalidCount()).isEqualTo(invalid);
        assertThat(log.getDuplicateCount()).isEqualTo(duplicate);
    }
}
