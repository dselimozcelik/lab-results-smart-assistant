package com.hospital.backend.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Externalised JWT config. Secret and expiry come from application config, not code.
@ConfigurationProperties(prefix = "lab.jwt")
public record JwtProperties(
        String secret,
        long expiryMinutes
) {
}
