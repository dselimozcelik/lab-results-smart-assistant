-- Move from "one row = one test" to "one tube (sample) holds many tests".
-- A real analyser processes a tube and reports a PANEL: one patient, one sampleId,
-- one measuredAt, several tests. This migration introduces the sample (tube) table and
-- rebuilds lab_result as its child. Demo data is disposable (no real patients), so we
-- drop and recreate rather than do an online backfill. In production this would be a
-- multi-step migration (add nullable FK -> backfill a sample per old sampleId -> set
-- NOT NULL -> drop old columns) to avoid any data loss or downtime.

-- The tube. sample_id is the device's business identifier and is unique: the same tube
-- is never ingested twice (duplicate tubes are logged to the audit table, not inserted).
CREATE TABLE sample (
    id          BIGSERIAL PRIMARY KEY,
    sample_id   VARCHAR(64)  NOT NULL UNIQUE,
    patient_id  VARCHAR(64)  NOT NULL,
    measured_at TIMESTAMPTZ  NOT NULL,
    device_id   VARCHAR(64)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_sample_patient_id  ON sample (patient_id);
CREATE INDEX idx_sample_measured_at ON sample (measured_at);

-- ai_analysis references lab_result(id); clear it before dropping lab_result so the FK
-- does not block the drop and no rows are left pointing at deleted tests. V8 will re-key
-- ai_analysis to the sample table.
TRUNCATE TABLE ai_analysis;

-- Rebuild lab_result as a child of sample. Tube-level fields (patient_id, measured_at,
-- device_id) now live on sample. value and the reference bounds are NULLABLE: a test that
-- the device reported but that is unusable is stored with anomaly_status = 'INVALID' (and
-- possibly no value) instead of being silently dropped, so a doctor can see it arrived.
-- test_code/test_name/unit stay NOT NULL: a test always identifies which test it is.
DROP TABLE lab_result CASCADE;

CREATE TABLE lab_result (
    id             BIGSERIAL PRIMARY KEY,
    sample_fk      BIGINT       NOT NULL REFERENCES sample (id),
    test_code      VARCHAR(32)  NOT NULL,
    test_name      VARCHAR(128) NOT NULL,
    value          DOUBLE PRECISION,
    unit           VARCHAR(32)  NOT NULL,
    reference_min  DOUBLE PRECISION,
    reference_max  DOUBLE PRECISION,
    anomaly_status VARCHAR(16)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- A given test appears at most once per tube; a repeated test_code in the same tube
    -- is a genuine duplicate and is rejected at ingestion time.
    CONSTRAINT uq_lab_result_sample_test UNIQUE (sample_fk, test_code)
);

CREATE INDEX idx_lab_result_sample_fk ON lab_result (sample_fk);
