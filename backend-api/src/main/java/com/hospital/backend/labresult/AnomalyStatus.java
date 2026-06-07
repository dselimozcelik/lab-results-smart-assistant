package com.hospital.backend.labresult;

// Classification of a single lab result against its reference range.
// Heuristic, not clinical truth (see CLAUDE.md business rules).
public enum AnomalyStatus {
    NORMAL,
    LOW,
    HIGH,
    CRITICAL,
    INVALID
}
