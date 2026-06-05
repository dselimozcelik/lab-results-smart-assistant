package com.hospital.backend.labresult;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

// Semantic (domain) validation beyond "is the field present". Returns the reason a
// record is invalid, or empty if it is acceptable. Bean-validation handles nulls first.
@Component
public class LabResultValidator {

    // Recognised measurement units. In production this would be per-test config.
    private static final Set<String> KNOWN_UNITS =
            Set.of("mg/dL", "mmol/L", "g/dL", "10^9/L", "%", "U/L");

    // A measurement older than this is treated as stale.
    private static final Duration MAX_AGE = Duration.ofDays(180);

    public Optional<String> findViolation(DeviceResultDto dto) {
        if (!KNOWN_UNITS.contains(dto.unit())) {
            return Optional.of("invalid-unit: " + dto.unit());
        }
        if (dto.referenceMin() > dto.referenceMax()) {
            return Optional.of("reference-bounds: min > max");
        }
        Instant now = Instant.now();
        if (dto.measuredAt().isAfter(now)) {
            return Optional.of("measuredAt in the future");
        }
        if (dto.measuredAt().isBefore(now.minus(MAX_AGE))) {
            return Optional.of("measuredAt too stale");
        }
        return Optional.empty();
    }
}
