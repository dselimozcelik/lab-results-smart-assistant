package com.hospital.backend.labresult;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

// Incoming payload from the mock device. Bean-validation annotations catch
// structurally missing fields; semantic checks live in domain validation.
public record DeviceResultDto(
        @NotBlank String sampleId,
        @NotBlank String patientId,
        @NotBlank String testCode,
        @NotBlank String testName,
        @NotNull Double value,
        @NotBlank String unit,
        @NotNull Double referenceMin,
        @NotNull Double referenceMax,
        @NotNull Instant measuredAt,
        @NotBlank String deviceId
) {
}
