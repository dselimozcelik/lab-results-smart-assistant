package com.hospital.backend.labresult;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

// Test-level semantic validation within a tube. Returns the reason a test is invalid, or empty
// if it is acceptable. A test that fails here is stored with anomalyStatus = INVALID, not dropped.
@Component
public class TestResultValidator {

    static final String INVALID_UNIT_PLACEHOLDER = "INVALID";
    static final int MAX_UNIT_LENGTH = 32;

    // Recognised measurement units. In production this would be per-test config.
    private static final Set<String> KNOWN_UNITS =
            Set.of("mg/dL", "mmol/L", "g/dL", "10^9/L", "%", "U/L");

    // Test codes whose value may legitimately be negative (e.g. base excess). Empty for the
    // current catalogue: every test here is a concentration/count that cannot be below zero,
    // so a negative reading is a device fault, not a low result. Kept as an allowlist so adding
    // a genuinely-signed test later is one line, not a rewrite of the rule.
    private static final Set<String> ALLOWS_NEGATIVE = Set.of();

    public Optional<String> findViolation(TestResultDto test) {
        if (test.value() == null) {
            return Optional.of("missing value");
        }
        if (!Double.isFinite(test.value())) {
            return Optional.of("non-finite value");
        }
        // A negative concentration/count is physically impossible: treat it as a device fault
        // (INVALID), not a very low value that would otherwise be classified CRITICAL.
        if (test.value() < 0 && !ALLOWS_NEGATIVE.contains(test.testCode())) {
            return Optional.of("non-physical: negative value");
        }
        if (test.unit() == null || test.unit().length() > MAX_UNIT_LENGTH || !KNOWN_UNITS.contains(test.unit())) {
            return Optional.of("invalid-unit: " + test.unit());
        }
        if (test.referenceMin() == null || test.referenceMax() == null) {
            return Optional.of("missing reference bounds");
        }
        if (!Double.isFinite(test.referenceMin()) || !Double.isFinite(test.referenceMax())) {
            return Optional.of("non-finite reference bounds");
        }
        if (test.referenceMin() > test.referenceMax()) {
            return Optional.of("reference-bounds: min > max");
        }
        return Optional.empty();
    }
}
