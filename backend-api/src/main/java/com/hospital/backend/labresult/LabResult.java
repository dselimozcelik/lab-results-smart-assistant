package com.hospital.backend.labresult;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

// One test within a tube. Belongs to a Sample (the tube). Maps 1:1 to the lab_result table.
// value and reference bounds are nullable: a test the device reported but that is unusable is
// stored with anomalyStatus = INVALID rather than dropped, so it stays visible to the doctor.
@Entity
@Table(name = "lab_result")
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sample_fk", nullable = false)
    private Sample sample;

    @Column(name = "test_code", nullable = false)
    private String testCode;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column
    private Double value;

    @Column(nullable = false)
    private String unit;

    @Column(name = "reference_min")
    private Double referenceMin;

    @Column(name = "reference_max")
    private Double referenceMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_status", nullable = false)
    private AnomalyStatus anomalyStatus;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected LabResult() {
        // JPA
    }

    public LabResult(String testCode, String testName, Double value, String unit,
                     Double referenceMin, Double referenceMax, AnomalyStatus anomalyStatus) {
        this.testCode = testCode;
        this.testName = testName;
        this.value = value;
        this.unit = unit;
        this.referenceMin = referenceMin;
        this.referenceMax = referenceMax;
        this.anomalyStatus = anomalyStatus;
    }

    void setSample(Sample sample) {
        this.sample = sample;
    }

    public Long getId() {
        return id;
    }

    public Sample getSample() {
        return sample;
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

    public AnomalyStatus getAnomalyStatus() {
        return anomalyStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
