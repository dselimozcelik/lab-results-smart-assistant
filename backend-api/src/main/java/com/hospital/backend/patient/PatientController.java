package com.hospital.backend.patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Patient-centric read API. Level 1 lists patients with a rollup; Level 2 returns one patient's
// tubes and their panels.
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Page<PatientSummaryResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return patientService.listPatients(pageable);
    }

    @GetMapping("/{patientId}")
    public PatientDetailResponse getOne(@PathVariable String patientId) {
        return patientService.getPatient(patientId);
    }
}
