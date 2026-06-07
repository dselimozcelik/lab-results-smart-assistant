package com.hospital.backend.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// One persisted summary per polling cycle. Maps 1:1 to the polling_audit_log table (Flyway-owned).
@Entity
@Table(name = "polling_audit_log")
public class PollingAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fetched_count", nullable = false)
    private int fetchedCount;

    @Column(name = "valid_count", nullable = false)
    private int validCount;

    @Column(name = "invalid_count", nullable = false)
    private int invalidCount;

    @Column(name = "duplicate_count", nullable = false)
    private int duplicateCount;

    @Column(name = "details")
    private String details;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected PollingAuditLog() {
        // JPA
    }

    public PollingAuditLog(int fetchedCount, int validCount, int invalidCount,
                           int duplicateCount, String details) {
        this.fetchedCount = fetchedCount;
        this.validCount = validCount;
        this.invalidCount = invalidCount;
        this.duplicateCount = duplicateCount;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public int getFetchedCount() {
        return fetchedCount;
    }

    public int getValidCount() {
        return validCount;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
