package com.hospital.backend.labresult;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// One tube/sample from the device: a single draw for one patient, holding a panel of tests.
// Maps 1:1 to the sample table (Flyway-owned). Tube-level facts live here; per-test facts
// live on LabResult.
@Entity
@Table(name = "sample")
public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_id", nullable = false, unique = true)
    private String sampleId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    // The panel. cascade=ALL + orphanRemoval lets saving the tube save all its tests in one go.
    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabResult> tests = new ArrayList<>();

    protected Sample() {
        // JPA
    }

    public Sample(String sampleId, String patientId, Instant measuredAt, String deviceId) {
        this.sampleId = sampleId;
        this.patientId = patientId;
        this.measuredAt = measuredAt;
        this.deviceId = deviceId;
    }

    // Keeps both sides of the relationship in sync so the FK is set on save.
    public void addTest(LabResult test) {
        tests.add(test);
        test.setSample(this);
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

    public Instant getMeasuredAt() {
        return measuredAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<LabResult> getTests() {
        return tests;
    }
}
