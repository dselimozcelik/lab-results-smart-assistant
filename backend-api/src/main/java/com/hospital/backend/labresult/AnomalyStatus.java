package com.hospital.backend.labresult;

// Classification of a single lab result against its reference range.
// A documented heuristic, not clinical truth; production would use per-test clinical thresholds.
public enum AnomalyStatus {
    NORMAL,
    LOW,
    HIGH,
    CRITICAL,
    INVALID
}
