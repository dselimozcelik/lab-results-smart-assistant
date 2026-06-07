package com.hospital.backend.patient;

import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.LabResult;
import com.hospital.backend.labresult.Sample;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SampleResponseTest {

    @Test
    void highOutranksLowRegardlessOfTestOrder() {
        Sample sample = new Sample("S-1", "P-1", Instant.now(), "DEV-1");
        sample.addTest(new LabResult("HIGH", "High", 12.0, "u", 1.0, 10.0, AnomalyStatus.HIGH));
        sample.addTest(new LabResult("LOW", "Low", 0.0, "u", 1.0, 10.0, AnomalyStatus.LOW));

        assertThat(SampleResponse.from(sample).worstStatus()).isEqualTo(AnomalyStatus.HIGH);
    }
}
