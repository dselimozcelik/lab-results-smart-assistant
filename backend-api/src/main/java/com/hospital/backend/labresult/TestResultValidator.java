package com.hospital.backend.labresult;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

// Test-level semantic validation within a tube. Returns the reason a test is invalid, or empty
// if it is acceptable. A test that fails here is stored with anomalyStatus = INVALID, not dropped.
@Component
public class TestResultValidator {

    // Recognised measurement units. In production this would be per-test config.
    private static final Set<String> KNOWN_UNITS =
            Set.of("mg/dL", "mmol/L", "g/dL", "10^9/L", "%", "U/L");

    public Optional<String> findViolation(TestResultDto test) {
        if (test.value() == null) {
            return Optional.of("missing value");
        }
        if (test.unit() == null || !KNOWN_UNITS.contains(test.unit())) {
            return Optional.of("invalid-unit: " + test.unit());
        }
        if (test.referenceMin() == null || test.referenceMax() == null) {
            return Optional.of("missing reference bounds");
        }
        if (test.referenceMin() > test.referenceMax()) {
            return Optional.of("reference-bounds: min > max");
        }
        return Optional.empty();
    }
}
