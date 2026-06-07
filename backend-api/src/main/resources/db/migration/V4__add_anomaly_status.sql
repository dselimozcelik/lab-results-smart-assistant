-- Anomaly classification for each stored result (computed by the backend, stored as text).
-- Existing rows (if any) get NORMAL so the column can be NOT NULL.
ALTER TABLE lab_result
    ADD COLUMN anomaly_status VARCHAR(16) NOT NULL DEFAULT 'NORMAL';

-- New rows must always carry an explicit status from the classifier; drop the default
-- so a missing value fails loudly instead of silently becoming NORMAL.
ALTER TABLE lab_result
    ALTER COLUMN anomaly_status DROP DEFAULT;
