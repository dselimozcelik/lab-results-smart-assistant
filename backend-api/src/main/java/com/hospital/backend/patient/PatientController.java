package com.hospital.backend.patient;

import com.hospital.backend.common.PageResponse;
import com.hospital.backend.labresult.AnomalyStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
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
    public PageResponse<PatientSummaryResponse> list(
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String testCode,
            @RequestParam(required = false) AnomalyStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(patientService.listPatients(patientId, testCode, status, from, to, pageable));
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
