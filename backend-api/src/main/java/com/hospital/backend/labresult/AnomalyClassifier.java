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

    public AnomalyStatus classify(double value, double referenceMin, double referenceMax) {
        if (referenceMin > referenceMax) {
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
