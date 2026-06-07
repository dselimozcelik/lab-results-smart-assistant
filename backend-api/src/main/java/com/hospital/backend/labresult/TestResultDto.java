package com.hospital.backend.labresult;

import jakarta.validation.constraints.NotBlank;

// One test inside a tube. testCode/testName identify the test and are required: a test with
// no identity cannot be stored meaningfully. value, unit and the reference bounds are NOT
// @NotNull on purpose — a test the device reported but that is unusable must still deserialize
// so ingestion can store it with anomalyStatus = INVALID rather than drop it.
public record TestResultDto(
        @NotBlank String testCode,
        @NotBlank String testName,
        String unit,
        Double value,
        Double referenceMin,
        Double referenceMax
) {
}
