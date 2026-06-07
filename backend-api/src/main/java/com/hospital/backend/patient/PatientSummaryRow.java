package com.hospital.backend.patient;

import java.time.Instant;

// Raw aggregation result per patient. worstSeverity is a numeric rank computed in SQL
// (higher = more severe); the service maps it back to an AnomalyStatus. Using an interface
// projection lets Spring Data fill it straight from the GROUP BY query.
public interface PatientSummaryRow {
    String getPatientId();
    long getTestCount();
    long getSampleCount();
    int getWorstSeverity();
    Instant getLastMeasuredAt();
}
