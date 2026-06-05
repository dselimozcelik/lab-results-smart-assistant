package com.hospital.backend.labresult;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// Processes one fetched batch: validate (structural + semantic), skip duplicates,
// persist the rest. Counts are logged; the audit table lands in Phase 3.
@Service
public class LabResultIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LabResultIngestionService.class);

    private final LabResultRepository repository;
    private final LabResultValidator validator;
    private final Validator beanValidator;

    public LabResultIngestionService(LabResultRepository repository,
                                     LabResultValidator validator,
                                     Validator beanValidator) {
        this.repository = repository;
        this.validator = validator;
        this.beanValidator = beanValidator;
    }

    @Transactional
    public void ingest(List<DeviceResultDto> batch) {
        int valid = 0, invalid = 0, duplicate = 0;

        for (DeviceResultDto dto : batch) {
            Optional<String> structural = structuralViolation(dto);
            if (structural.isPresent()) {
                invalid++;
                log.warn("Invalid record [{}]: {}", dto.sampleId(), structural.get());
                continue;
            }
            Optional<String> semantic = validator.findViolation(dto);
            if (semantic.isPresent()) {
                invalid++;
                log.warn("Invalid record [{}]: {}", dto.sampleId(), semantic.get());
                continue;
            }
            if (repository.existsBySampleId(dto.sampleId())) {
                duplicate++;
                log.info("Duplicate record skipped [{}]", dto.sampleId());
                continue;
            }
            try {
                repository.save(toEntity(dto));
                valid++;
            } catch (DataIntegrityViolationException e) {
                // Lost the race on the unique constraint: treat as duplicate.
                duplicate++;
                log.info("Duplicate record skipped (constraint) [{}]", dto.sampleId());
            }
        }

        log.info("Ingest cycle: fetched={} valid={} invalid={} duplicate={}",
                batch.size(), valid, invalid, duplicate);
    }

    private Optional<String> structuralViolation(DeviceResultDto dto) {
        Set<ConstraintViolation<DeviceResultDto>> violations = beanValidator.validate(dto);
        if (violations.isEmpty()) {
            return Optional.empty();
        }
        String detail = violations.iterator().next().getPropertyPath()
                + " " + violations.iterator().next().getMessage();
        return Optional.of("missing-field: " + detail);
    }

    private LabResult toEntity(DeviceResultDto d) {
        return new LabResult(d.sampleId(), d.patientId(), d.testCode(), d.testName(),
                d.value(), d.unit(), d.referenceMin(), d.referenceMax(),
                d.measuredAt(), d.deviceId());
    }
}
