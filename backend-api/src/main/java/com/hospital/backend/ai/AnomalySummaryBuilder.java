package com.hospital.backend.ai;

import com.hospital.backend.labresult.LabResult;
import org.springframework.stereotype.Component;

// Builds a deterministic, factual summary of a result for the LLM prompt.
// The backend states the numbers and the computed status; the model only interprets them,
// so it can never invent reference ranges or re-classify the value.
@Component
public class AnomalySummaryBuilder {

    public String build(LabResult r) {
        return """
                Test: %s (%s)
                Measured value: %s %s
                Reference range: %s - %s %s
                Computed status: %s"""
                .formatted(
                        r.getTestName(), r.getTestCode(),
                        r.getValue(), r.getUnit(),
                        r.getReferenceMin(), r.getReferenceMax(), r.getUnit(),
                        r.getAnomalyStatus());
    }
}
