-- One row per polling cycle: a summary of what the poller fetched and what happened
-- to each record (valid / invalid / duplicate). Lets us audit the ingestion over time.
CREATE TABLE polling_audit_log (
    id              BIGSERIAL PRIMARY KEY,
    fetched_count   INTEGER     NOT NULL,
    valid_count     INTEGER     NOT NULL,
    invalid_count   INTEGER     NOT NULL,
    duplicate_count INTEGER     NOT NULL,
    details         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_polling_audit_log_created_at ON polling_audit_log (created_at);
