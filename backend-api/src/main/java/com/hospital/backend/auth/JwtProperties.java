package com.hospital.backend.auth;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Externalised JWT config. Secret and expiry come from application config, not code.
@Validated
@ConfigurationProperties(prefix = "lab.jwt")
public record JwtProperties(
        @NotBlank
        @Size(min = 32, message = "must contain at least 32 characters")
        String secret,
        @Min(1)
        long expiryMinutes
) {
}
