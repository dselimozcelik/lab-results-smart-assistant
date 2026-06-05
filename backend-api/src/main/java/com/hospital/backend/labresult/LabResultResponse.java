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
        Instant createdAt
) {
    public static LabResultResponse from(LabResult e) {
        return new LabResultResponse(
                e.getId(), e.getSampleId(), e.getPatientId(), e.getTestCode(), e.getTestName(),
                e.getValue(), e.getUnit(), e.getReferenceMin(), e.getReferenceMax(),
                e.getMeasuredAt(), e.getDeviceId(), e.getCreatedAt());
    }
}
