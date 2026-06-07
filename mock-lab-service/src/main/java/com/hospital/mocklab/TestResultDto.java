package com.hospital.mocklab;

// One test inside a tube. The JSON shape must match the backend's TestResultDto.
// Fields are nullable so we can simulate missing-value / invalid-unit scenarios.
public record TestResultDto(
        String testCode,
        String testName,
        String unit,
        Double value,
        Double referenceMin,
        Double referenceMax
) {
}
