package com.hospital.mocklab;

import java.util.List;

// A catalogue of realistic lab tests with their reference ranges, units and device.
// Used by the factory to build varied, plausible measurements.
public enum TestType {
    GLUCOSE("GLU", "Glucose", "mg/dL", 70.0, 110.0, "DEV-1"),
    POTASSIUM("K", "Potassium", "mmol/L", 3.5, 5.1, "DEV-2"),
    SODIUM("NA", "Sodium", "mmol/L", 135.0, 145.0, "DEV-2"),
    HEMOGLOBIN("HGB", "Hemoglobin", "g/dL", 12.0, 17.5, "DEV-3"),
    WBC("WBC", "White Blood Cell Count", "10^9/L", 4.0, 11.0, "DEV-3"),
    CREATININE("CREA", "Creatinine", "mg/dL", 0.6, 1.3, "DEV-1");

    final String code;
    final String name;
    final String unit;
    final double refMin;
    final double refMax;
    final String deviceId;

    TestType(String code, String name, String unit, double refMin, double refMax, String deviceId) {
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.refMin = refMin;
        this.refMax = refMax;
        this.deviceId = deviceId;
    }

    static final List<TestType> ALL = List.of(values());
}
