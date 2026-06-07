package com.hospital.backend.patient;

import com.hospital.backend.labresult.AnomalyStatus;
import com.hospital.backend.labresult.LabResult;
import com.hospital.backend.labresult.LabResultResponse;
import com.hospital.backend.labresult.Sample;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

// Level-2 nested view: one tube and the panel of tests it carries. worstStatus is the most
// severe status in this tube (abnormal-first ordering of the tests themselves is left to the UI).
public record SampleResponse(
        String sampleId,
        Instant measuredAt,
        String deviceId,
        AnomalyStatus worstStatus,
        List<LabResultResponse> tests
) {
    public static SampleResponse from(Sample s) {
        List<LabResultResponse> tests = s.getTests().stream()
                .map(LabResultResponse::from)
                .toList();
        AnomalyStatus worst = s.getTests().stream()
                .map(LabResult::getAnomalyStatus)
                .max(Comparator.comparingInt(SampleResponse::severity))
                .orElse(AnomalyStatus.NORMAL);
        return new SampleResponse(s.getSampleId(), s.getMeasuredAt(), s.getDeviceId(), worst, tests);
    }

    private static int severity(AnomalyStatus status) {
        return switch (status) {
            case CRITICAL -> 4;
            case HIGH, LOW -> 3;
            case INVALID -> 2;
            case NORMAL -> 1;
        };
    }
}
