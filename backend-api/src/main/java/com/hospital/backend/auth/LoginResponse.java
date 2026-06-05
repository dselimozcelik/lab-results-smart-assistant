package com.hospital.backend.auth;

public record LoginResponse(
        String token,
        String username,
        String role,
        long expiresInMinutes
) {
}
