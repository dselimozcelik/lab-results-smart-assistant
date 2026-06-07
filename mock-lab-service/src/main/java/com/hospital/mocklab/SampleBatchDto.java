package com.hospital.mocklab;

import java.time.Instant;
import java.util.List;

// One tube as the device emits it: tube-level facts plus the panel of tests. The JSON shape
// must match the backend's SampleBatchDto. Fields are nullable so we can simulate bad data.
public record SampleBatchDto(
        String sampleId,
        String patientId,
        Instant measuredAt,
        String deviceId,
        List<TestResultDto> tests
) {
}
