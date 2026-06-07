package com.hospital.backend.labresult;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

// Tube-level validation: facts about the draw itself. If the tube's time is untrustworthy,
// the whole tube is rejected at ingestion (its results can't be trusted either). Returns the
// reason a tube is invalid, or empty if it is acceptable.
@Component
public class SampleValidator {

    // A draw older than this is treated as stale.
    private static final Duration MAX_AGE = Duration.ofDays(180);

    public Optional<String> findViolation(SampleBatchDto tube) {
        Instant now = Instant.now();
        if (tube.measuredAt().isAfter(now)) {
            return Optional.of("measuredAt in the future");
        }
        if (tube.measuredAt().isBefore(now.minus(MAX_AGE))) {
            return Optional.of("measuredAt too stale");
        }
        return Optional.empty();
    }
}
