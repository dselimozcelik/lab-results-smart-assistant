package com.hospital.backend.labresult;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// Persisted, validated lab measurement. Maps 1:1 to the lab_result table (Flyway-owned).
@Entity
@Table(name = "lab_result")
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_id", nullable = false, unique = true)
    private String sampleId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "test_code", nullable = false)
    private String testCode;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private String unit;

    @Column(name = "reference_min", nullable = false)
    private Double referenceMin;

    @Column(name = "reference_max", nullable = false)
    private Double referenceMax;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_status", nullable = false)
    private AnomalyStatus anomalyStatus;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected LabResult() {
        // JPA
    }

    public LabResult(String sampleId, String patientId, String testCode, String testName,
                     Double value, String unit, Double referenceMin, Double referenceMax,
                     Instant measuredAt, String deviceId, AnomalyStatus anomalyStatus) {
        this.sampleId = sampleId;
        this.patientId = patientId;
        this.testCode = testCode;
        this.testName = testName;
        this.value = value;
        this.unit = unit;
        this.referenceMin = referenceMin;
        this.referenceMax = referenceMax;
        this.measuredAt = measuredAt;
        this.deviceId = deviceId;
        this.anomalyStatus = anomalyStatus;
    }

    public Long getId() {
        return id;
    }

    public String getSampleId() {
        return sampleId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getTestCode() {
        return testCode;
    }

    public String getTestName() {
        return testName;
    }

    public Double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public Double getReferenceMin() {
        return referenceMin;
    }

    public Double getReferenceMax() {
        return referenceMax;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public AnomalyStatus getAnomalyStatus() {
        return anomalyStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
