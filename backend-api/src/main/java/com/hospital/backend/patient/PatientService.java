package com.hospital.backend.patient;

import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.Sample;
import com.hospital.backend.labresult.SampleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Read side of the patient-centric views. Turns the SQL aggregation into Level-1 summaries and
// assembles a patient's tubes into a Level-2 detail.
@Service
public class PatientService {

    private final SampleRepository sampleRepository;

    public PatientService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @Transactional(readOnly = true)
    public Page<PatientSummaryResponse> listPatients(
            String patientId,
            String testCode,
            AnomalyStatus status,
            Instant from,
            Instant to,
            Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
        return sampleRepository.findPatientSummaries(
                        optionalText(patientId), optionalText(testCode), status, from, to, pageable)
                .map(row -> new PatientSummaryResponse(
                        row.getPatientId(),
                        row.getTestCount(),
                        row.getSampleCount(),
                        fromSeverity(row.getWorstSeverity(), row.getHighCount()),
                        row.getLastMeasuredAt()));
    }

    private static final int HISTORY_LIMIT = 12;

    @Transactional(readOnly = true)
    public List<TestHistoryPoint> getTestHistory(String patientId, String testCode) {
        // The query returns newest-first (so the limit keeps the most recent); a sparkline reads
        // left-to-right in time, so reverse to chronological order before returning.
        List<TestHistoryPoint> recent = sampleRepository.findTestHistory(
                patientId, testCode, PageRequest.of(0, HISTORY_LIMIT));
        List<TestHistoryPoint> chronological = new ArrayList<>(recent);
        Collections.reverse(chronological);
        return chronological;
    }

    @Transactional(readOnly = true)
    public List<String> suggestPatientIds(String prefix, int limit) {
        String trimmed = prefix == null ? "" : prefix.trim();
        if (trimmed.length() < 2) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return sampleRepository.findPatientIdSuggestions(trimmed, PageRequest.of(0, safeLimit));
    }

    @Transactional(readOnly = true)
    public PatientDetailResponse getPatient(String patientId) {
        // JOIN FETCH can repeat a tube once per test row; dedupe by sample id, preserving order.
        Map<String, Sample> uniqueTubes = new LinkedHashMap<>();
        for (Sample s : sampleRepository.findByPatientIdWithTests(patientId)) {
            uniqueTubes.putIfAbsent(s.getSampleId(), s);
        }
        if (uniqueTubes.isEmpty()) {
            throw new EntityNotFoundException("Patient not found: " + patientId);
        }
        List<SampleResponse> samples = uniqueTubes.values().stream()
                .map(SampleResponse::from)
                .toList();
        return new PatientDetailResponse(patientId, samples);
    }

    private String optionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    // HIGH and LOW share the same severity; highCount preserves the correct label.
    private AnomalyStatus fromSeverity(int severity, long highCount) {
        return switch (severity) {
            case 4 -> AnomalyStatus.CRITICAL;
            case 3 -> highCount > 0 ? AnomalyStatus.HIGH : AnomalyStatus.LOW;
            case 2 -> AnomalyStatus.INVALID;
            default -> AnomalyStatus.NORMAL;
        };
    }
}
