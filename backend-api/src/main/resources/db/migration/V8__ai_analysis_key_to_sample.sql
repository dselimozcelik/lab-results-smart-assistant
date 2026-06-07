-- AI analysis moves from per-test to per-tube (panel). A real review reads the whole panel
-- from one draw together, so the analysis is keyed by the sample (tube), not a single test.
-- V7 already TRUNCATEd ai_analysis, so re-keying loses no real data.

ALTER TABLE ai_analysis DROP CONSTRAINT uq_ai_analysis_result_model_prompt;
DROP INDEX idx_ai_analysis_lab_result_id;
ALTER TABLE ai_analysis DROP COLUMN lab_result_id;

ALTER TABLE ai_analysis
    ADD COLUMN sample_fk BIGINT NOT NULL REFERENCES sample (id);

-- One cached analysis per (tube, model, prompt version): re-requesting the same panel is a cache hit.
ALTER TABLE ai_analysis
    ADD CONSTRAINT uq_ai_analysis_sample_model_prompt UNIQUE (sample_fk, model, prompt_version);

CREATE INDEX idx_ai_analysis_sample_fk ON ai_analysis (sample_fk);
