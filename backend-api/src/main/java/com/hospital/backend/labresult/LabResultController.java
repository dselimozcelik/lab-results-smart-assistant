package com.hospital.backend.labresult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/lab-results")
public class LabResultController {

    private final LabResultRepository repository;

    public LabResultController(LabResultRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<LabResultResponse> list(
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String testCode,
            @RequestParam(required = false) AnomalyStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        return repository.search(patientId, testCode, status, from, to, pageable)
                .map(LabResultResponse::from);
    }
}
