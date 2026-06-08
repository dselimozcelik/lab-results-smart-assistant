package com.hospital.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // A >= 256-bit secret, as HS256 requires.
    private static final String SECRET = "test-secret-please-change-32bytes-minimum-0123456789";

    private final JwtService service = new JwtService(new JwtProperties(SECRET, 60));

    @Test
    void issuedTokenRoundTripsToTheSameSubjectAndRole() {
        String token = service.issue("doctor", "DOCTOR");

        Claims claims = service.parse(token);

        assertThat(claims.getSubject()).isEqualTo("doctor");
        assertThat(claims.get("role", String.class)).isEqualTo("DOCTOR");
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        // Forged: signed by a different key. Verification with our key must fail.
        String foreign = new JwtService(new JwtProperties("a-totally-different-secret-32bytes-min-0123456789", 60))
                .issue("doctor", "DOCTOR");

        assertThatThrownBy(() -> service.parse(foreign)).isInstanceOf(JwtException.class);
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = service.issue("doctor", "DOCTOR");
        // Flip a character in the signature segment.
        String tampered = token.substring(0, token.length() - 2)
                + (token.endsWith("a") ? "b" : "a");

        assertThatThrownBy(() -> service.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        // Negative expiry => the token is already expired the moment it is issued.
        JwtService shortLived = new JwtService(new JwtProperties(SECRET, -1));
        String token = shortLived.issue("doctor", "DOCTOR");

        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void garbageStringIsRejected() {
        assertThatThrownBy(() -> service.parse("not-a-jwt")).isInstanceOf(JwtException.class);
    }
}
