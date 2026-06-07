package com.hospital.backend.labresult;

import com.hospital.backend.audit.PollingAuditLog;
import com.hospital.backend.audit.PollingAuditLogRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Processes one fetched batch: validate (structural + semantic), skip duplicates,
// classify anomalies, persist the rest. Each cycle's counts are saved to the audit log.
@Service
public class LabResultIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LabResultIngestionService.class);

    private final LabResultRepository repository;
    private final LabResultValidator validator;
    private final Validator beanValidator;
    private final AnomalyClassifier anomalyClassifier;
    private final PollingAuditLogRepository auditLogRepository;

    public LabResultIngestionService(LabResultRepository repository,
                                     LabResultValidator validator,
                                     Validator beanValidator,
                                     AnomalyClassifier anomalyClassifier,
                                     PollingAuditLogRepository auditLogRepository) {
        this.repository = repository;
        this.validator = validator;
        this.beanValidator = beanValidator;
        this.anomalyClassifier = anomalyClassifier;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void ingest(List<DeviceResultDto> batch) {
        int valid = 0, invalid = 0, duplicate = 0;

        // sampleIds already accepted in THIS batch, so a repeat inside one batch
        // is caught before it can hit the DB unique constraint.
        Set<String> seenInBatch = new HashSet<>();

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
            if (seenInBatch.contains(dto.sampleId()) || repository.existsBySampleId(dto.sampleId())) {
                duplicate++;
                log.info("Duplicate record skipped [{}]", dto.sampleId());
                continue;
            }
            repository.save(toEntity(dto));
            seenInBatch.add(dto.sampleId());
            valid++;
        }

        log.info("Ingest cycle: fetched={} valid={} invalid={} duplicate={}",
                batch.size(), valid, invalid, duplicate);

        auditLogRepository.save(
                new PollingAuditLog(batch.size(), valid, invalid, duplicate, null));
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
        AnomalyStatus status = anomalyClassifier.classify(d.value(), d.referenceMin(), d.referenceMax());
        return new LabResult(d.sampleId(), d.patientId(), d.testCode(), d.testName(),
                d.value(), d.unit(), d.referenceMin(), d.referenceMax(),
                d.measuredAt(), d.deviceId(), status);
    }
}
