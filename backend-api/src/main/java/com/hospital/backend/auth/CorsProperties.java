package com.hospital.backend.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "lab.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
