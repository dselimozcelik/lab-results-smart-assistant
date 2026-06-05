package com.hospital.backend.labresult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lab-results")
public class LabResultController {

    private final LabResultRepository repository;

    public LabResultController(LabResultRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<LabResultResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return repository.findAll(pageable).map(LabResultResponse::from);
    }
}
