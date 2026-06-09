package com.hospital.backend.labresult;

import org.springframework.stereotype.Component;

// Classifies a valid lab result against its reference range.
// Invalid records never reach here (LabResultValidator filters them out first),
// so INVALID is only a defensive fallback for impossible bounds.
@Component
public class AnomalyClassifier {

    private final double criticalFactor;

    public AnomalyClassifier(AnomalyProperties props) {
        this.criticalFactor = props.criticalFactor();
    }

    // Null-safe entry point: a test with a missing value or missing bounds is INVALID, not an
    // error. Otherwise delegates to the primitive classification below.
    public AnomalyStatus classify(Double value, Double referenceMin, Double referenceMax) {
        if (value == null || referenceMin == null || referenceMax == null) {
            return AnomalyStatus.INVALID;
        }
        return classify(value.doubleValue(), referenceMin.doubleValue(), referenceMax.doubleValue());
    }

    public AnomalyStatus classify(double value, double referenceMin, double referenceMax) {
        if (!Double.isFinite(value) || !Double.isFinite(referenceMin) || !Double.isFinite(referenceMax)
                || referenceMin > referenceMax) {
            return AnomalyStatus.INVALID;
        }

        double rangeWidth = referenceMax - referenceMin;
        double margin = criticalFactor * rangeWidth;

        if (value < referenceMin - margin || value > referenceMax + margin) {
            return AnomalyStatus.CRITICAL;
        }
        if (value < referenceMin) {
            return AnomalyStatus.LOW;
        }
        if (value > referenceMax) {
            return AnomalyStatus.HIGH;
        }
        return AnomalyStatus.NORMAL;
    }
}
