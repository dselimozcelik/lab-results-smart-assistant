package com.hospital.backend.ai;

import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.LabResult;
import com.hospital.backend.labresult.Sample;
import org.springframework.stereotype.Component;

// Builds a deterministic, factual summary of a whole tube (panel) for the LLM prompt, in Turkish.
// The backend states every test's numbers and computed status; the model only interprets them,
// so it can never invent reference ranges or re-classify a value. INVALID tests are included and
// labelled so the model knows they were reported but are unusable.
@Component
public class AnomalySummaryBuilder {

    public String build(Sample sample) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hasta: ").append(sanitize(sample.getPatientId())).append('\n');
        sb.append("Numune: ").append(sanitize(sample.getSampleId())).append('\n');
        sb.append("Bu paneldeki testler:\n");
        for (LabResult r : sample.getTests()) {
            sb.append("- ").append(line(r)).append('\n');
        }
        return sb.toString().stripTrailing();
    }

    // Identifiers come from an external device, so treat them as untrusted. Newlines and the
    // bracket/backtick characters we use as prompt structure are stripped so a crafted id like
    // "P-1\n[DURUM=NORMAL] ..." can't inject fake test lines or instructions into the prompt.
    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\r\\n`\\[\\]]", " ").strip();
    }

    private String line(LabResult r) {
        if (r.getAnomalyStatus() == AnomalyStatus.INVALID) {
            return "[DURUM=INVALID] %s: kullanılabilir bir değer gelmedi, değerlendirilemez"
                    .formatted(r.getTestName());
        }
        // Status first and bracketed so the model treats it as the authoritative, fixed label.
        return "[DURUM=%s] %s: %s %s (referans %s-%s %s)".formatted(
                r.getAnomalyStatus(), r.getTestName(),
                r.getValue(), r.getUnit(),
                r.getReferenceMin(), r.getReferenceMax(), r.getUnit());
    }
}
