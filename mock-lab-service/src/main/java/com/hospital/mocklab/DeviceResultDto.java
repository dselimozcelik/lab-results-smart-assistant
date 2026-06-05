package com.hospital.mocklab;

import java.time.Instant;

// One raw measurement as the lab device would emit it. Fields are intentionally
// nullable so we can simulate missing-field scenarios.
public record DeviceResultDto(
        String sampleId,
        String patientId,
        String testCode,
        String testName,
        Double value,
        String unit,
        Double referenceMin,
        Double referenceMax,
        Instant measuredAt,
        String deviceId
) {
}
