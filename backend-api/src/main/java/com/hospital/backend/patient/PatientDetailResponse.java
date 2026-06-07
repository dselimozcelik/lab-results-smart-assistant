package com.hospital.backend.patient;

import java.util.List;

// Level-2 view: a patient and their tubes (most recent first), each tube holding its panel.
public record PatientDetailResponse(
        String patientId,
        List<SampleResponse> samples
) {
}
