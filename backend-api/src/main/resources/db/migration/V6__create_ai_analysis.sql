-- One cached AI analysis per (lab result, model, prompt version). The unique constraint
-- makes re-requesting the same analysis a cache hit instead of another LLM call.
CREATE TABLE ai_analysis (
    id                  BIGSERIAL PRIMARY KEY,
    lab_result_id       BIGINT       NOT NULL REFERENCES lab_result (id),
    model               VARCHAR(64)  NOT NULL,
    prompt_version      VARCHAR(32)  NOT NULL,
    summary             TEXT         NOT NULL,
    flagged_tests       TEXT         NOT NULL,
    suggested_followups TEXT         NOT NULL,
    disclaimer          TEXT         NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_ai_analysis_result_model_prompt
        UNIQUE (lab_result_id, model, prompt_version)
);

CREATE INDEX idx_ai_analysis_lab_result_id ON ai_analysis (lab_result_id);
