package com.hospital.backend.labresult;

import java.time.Instant;

// API-facing view of a stored result. Keeps the JPA entity out of the web layer.
public record LabResultResponse(
        Long id,
        String sampleId,
        String patientId,
        String testCode,
        String testName,
        Double value,
        String unit,
        Double referenceMin,
        Double referenceMax,
        Instant measuredAt,
        String deviceId,
        AnomalyStatus anomalyStatus,
        Instant createdAt
) {
    public static LabResultResponse from(LabResult e) {
        Sample s = e.getSample();
        return new LabResultResponse(
                e.getId(), s.getSampleId(), s.getPatientId(), e.getTestCode(), e.getTestName(),
                e.getValue(), e.getUnit(), e.getReferenceMin(), e.getReferenceMax(),
                s.getMeasuredAt(), s.getDeviceId(), e.getAnomalyStatus(), e.getCreatedAt());
    }
}
