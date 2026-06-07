package com.hospital.backend.labresult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

// One tube from the device: tube-level facts plus the panel of tests it carries.
// Bean-validation here catches a structurally broken tube envelope; a single broken
// TEST inside is handled at ingestion (stored as INVALID), not rejected here.
public record SampleBatchDto(
        @NotBlank String sampleId,
        @NotBlank String patientId,
        @NotNull Instant measuredAt,
        @NotBlank String deviceId,
        @NotEmpty @Valid List<TestResultDto> tests
) {
}
