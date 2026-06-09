package com.hospital.backend.labresult;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnomalyClassifierTest {

    // criticalFactor 0.5: range [70,110] (width 40) => critical margin 20.
    private final AnomalyClassifier classifier = new AnomalyClassifier(new AnomalyProperties(0.5));

    @Test
    void inRangeIsNormal() {
        assertThat(classifier.classify(95.0, 70.0, 110.0)).isEqualTo(AnomalyStatus.NORMAL);
    }

    @Test
    void belowMinIsLow() {
        assertThat(classifier.classify(65.0, 70.0, 110.0)).isEqualTo(AnomalyStatus.LOW);
    }

    @Test
    void aboveMaxIsHigh() {
        assertThat(classifier.classify(120.0, 70.0, 110.0)).isEqualTo(AnomalyStatus.HIGH);
    }

    @Test
    void farBeyondBoundIsCritical() {
        assertThat(classifier.classify(140.0, 70.0, 110.0)).isEqualTo(AnomalyStatus.CRITICAL);
        assertThat(classifier.classify(40.0, 70.0, 110.0)).isEqualTo(AnomalyStatus.CRITICAL);
    }

    @Test
    void nullValueOrBoundsIsInvalid() {
        assertThat(classifier.classify(null, 70.0, 110.0)).isEqualTo(AnomalyStatus.INVALID);
        assertThat(classifier.classify(95.0, null, 110.0)).isEqualTo(AnomalyStatus.INVALID);
        assertThat(classifier.classify(95.0, 70.0, null)).isEqualTo(AnomalyStatus.INVALID);
    }

    @Test
    void invertedBoundsIsInvalid() {
        assertThat(classifier.classify(95.0, 110.0, 70.0)).isEqualTo(AnomalyStatus.INVALID);
    }

    @Test
    void nonFiniteValueOrBoundsAreInvalid() {
        assertThat(classifier.classify(Double.NaN, 70.0, 110.0)).isEqualTo(AnomalyStatus.INVALID);
        assertThat(classifier.classify(Double.POSITIVE_INFINITY, 70.0, 110.0))
                .isEqualTo(AnomalyStatus.INVALID);
        assertThat(classifier.classify(95.0, Double.NEGATIVE_INFINITY, 110.0))
                .isEqualTo(AnomalyStatus.INVALID);
    }
}
