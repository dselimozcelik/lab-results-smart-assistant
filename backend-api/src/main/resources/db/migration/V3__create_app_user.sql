CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(32)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Demo doctor. password = "Doctor123!" (BCrypt). Credentials are documented in the README;
-- in production users would be provisioned by an admin, not seeded.
INSERT INTO app_user (username, password_hash, role)
VALUES ('doctor', '$2a$10$sIaemYy00UAKLf1AFLJj2uUvJGxSS4cTPebveBlB4Pl.V24tELdLC', 'DOCTOR');
