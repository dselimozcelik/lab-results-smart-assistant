package com.hospital.backend.patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Patient-centric read API. Level 1 lists patients with a rollup (optionally filtered by patientId
// prefix); Level 2 returns one patient's tubes and their panels.
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Page<PatientSummaryResponse> list(
            @RequestParam(required = false) String patientId,
            @PageableDefault(size = 20) Pageable pageable) {
        return patientService.listPatients(patientId, pageable);
    }

    @GetMapping("/suggestions")
    public List<String> suggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "8") int limit) {
        return patientService.suggestPatientIds(query, limit);
    }

    @GetMapping("/{patientId}")
    public PatientDetailResponse getOne(@PathVariable String patientId) {
        return patientService.getPatient(patientId);
    }
}
