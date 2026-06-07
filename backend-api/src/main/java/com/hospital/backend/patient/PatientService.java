package com.hospital.backend.patient;

import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.Sample;
import com.hospital.backend.labresult.SampleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<PatientSummaryResponse> listPatients(Pageable pageable) {
        return sampleRepository.findPatientSummaries(pageable)
                .map(row -> new PatientSummaryResponse(
                        row.getPatientId(),
                        row.getTestCount(),
                        row.getSampleCount(),
                        fromSeverity(row.getWorstSeverity()),
                        row.getLastMeasuredAt()));
    }

    @Transactional(readOnly = true)
    public PatientDetailResponse getPatient(String patientId) {
        // JOIN FETCH can repeat a tube once per test row; dedupe by sample id, preserving order.
        Map<String, Sample> uniqueTubes = new LinkedHashMap<>();
        for (Sample s : sampleRepository.findByPatientIdWithTests(patientId)) {
            uniqueTubes.putIfAbsent(s.getSampleId(), s);
        }
        List<SampleResponse> samples = uniqueTubes.values().stream()
                .map(SampleResponse::from)
                .toList();
        return new PatientDetailResponse(patientId, samples);
    }

    // Inverse of the severity ranking in the summary query.
    private AnomalyStatus fromSeverity(int severity) {
        return switch (severity) {
            case 4 -> AnomalyStatus.CRITICAL;
            case 3 -> AnomalyStatus.HIGH;
            case 2 -> AnomalyStatus.INVALID;
            default -> AnomalyStatus.NORMAL;
        };
    }
}
