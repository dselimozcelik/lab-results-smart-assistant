package com.hospital.backend.audit;

import java.time.Instant;

// API-facing view of one polling-cycle audit row. Keeps the JPA entity out of the web layer.
public record PollingAuditLogResponse(
        Long id,
        int fetchedCount,
        int validCount,
        int invalidCount,
        int duplicateCount,
        String details,
        Instant createdAt
) {
    public static PollingAuditLogResponse from(PollingAuditLog e) {
        return new PollingAuditLogResponse(
                e.getId(), e.getFetchedCount(), e.getValidCount(),
                e.getInvalidCount(), e.getDuplicateCount(), e.getDetails(), e.getCreatedAt());
    }
}
