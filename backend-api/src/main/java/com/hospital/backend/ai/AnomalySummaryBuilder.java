package com.hospital.backend.ai;

import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.LabResult;
import com.hospital.backend.labresult.Sample;
import org.springframework.stereotype.Component;

// Builds a deterministic, factual summary of a whole tube (panel) for the LLM prompt.
// The backend states every test's numbers and computed status; the model only interprets them,
// so it can never invent reference ranges or re-classify a value. INVALID tests are included and
// labelled so the model knows they were reported but are unusable.
@Component
public class AnomalySummaryBuilder {

    public String build(Sample sample) {
        StringBuilder sb = new StringBuilder();
        sb.append("Patient: ").append(sample.getPatientId()).append('\n');
        sb.append("Sample: ").append(sample.getSampleId()).append('\n');
        sb.append("Tests in this panel:\n");
        for (LabResult r : sample.getTests()) {
            sb.append("- ").append(line(r)).append('\n');
        }
        return sb.toString().stripTrailing();
    }

    private String line(LabResult r) {
        if (r.getAnomalyStatus() == AnomalyStatus.INVALID) {
            return "%s (%s): no usable value reported (INVALID)".formatted(r.getTestName(), r.getTestCode());
        }
        return "%s (%s): value %s %s, reference range %s - %s %s, status %s".formatted(
                r.getTestName(), r.getTestCode(),
                r.getValue(), r.getUnit(),
                r.getReferenceMin(), r.getReferenceMax(), r.getUnit(),
                r.getAnomalyStatus());
    }
}
