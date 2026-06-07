package com.hospital.backend.patient;

import com.hospital.backend.labresult.AnomalyStatus;

import java.time.Instant;

// Level-1 row: one patient with an at-a-glance rollup across all their tubes.
// worstStatus is the most severe status among the patient's tests (severity ranked in SQL).
public record PatientSummaryResponse(
        String patientId,
        long testCount,
        long sampleCount,
        AnomalyStatus worstStatus,
        Instant lastMeasuredAt
) {
}
