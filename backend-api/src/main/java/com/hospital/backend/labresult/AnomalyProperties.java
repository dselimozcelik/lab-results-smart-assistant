package com.hospital.backend.labresult;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Externalised anomaly config. criticalFactor is how far beyond a reference bound
// (as a fraction of the range width) counts as CRITICAL. No magic numbers in code.
@ConfigurationProperties(prefix = "lab.anomaly")
public record AnomalyProperties(
        double criticalFactor
) {
}
