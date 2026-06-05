CREATE TABLE lab_result (
    id             BIGSERIAL PRIMARY KEY,
    sample_id      VARCHAR(64)  NOT NULL UNIQUE,
    patient_id     VARCHAR(64)  NOT NULL,
    test_code      VARCHAR(32)  NOT NULL,
    test_name      VARCHAR(128) NOT NULL,
    value          DOUBLE PRECISION NOT NULL,
    unit           VARCHAR(32)  NOT NULL,
    reference_min  DOUBLE PRECISION NOT NULL,
    reference_max  DOUBLE PRECISION NOT NULL,
    measured_at    TIMESTAMPTZ  NOT NULL,
    device_id      VARCHAR(64)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_lab_result_patient_id ON lab_result (patient_id);
CREATE INDEX idx_lab_result_test_code  ON lab_result (test_code);
