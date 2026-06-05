package com.hospital.backend.labresult;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Externalised polling config. No magic numbers in code.
@ConfigurationProperties(prefix = "lab.polling")
public record PollingProperties(
        String mockBaseUrl,
        long fixedDelayMs,
        // Which device scenario to pull. Useful to demo invalid/critical/duplicate handling.
        String scenario
) {
}
