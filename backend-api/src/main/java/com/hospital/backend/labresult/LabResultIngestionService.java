package com.hospital.backend.labresult;

import com.hospital.backend.audit.PollingAuditService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Processes one fetched batch of tubes. For each tube: validate the envelope and the tube's
// metadata; skip duplicate tubes; then for each test inside, store valid tests classified and
// broken tests as INVALID (never dropped). Each cycle's counts are saved to the audit log.
// Audit count meaning in the panel model: fetched = tubes, valid/invalid = tests, duplicate = tubes.
@Service
public class LabResultIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LabResultIngestionService.class);

    private final SampleRepository sampleRepository;
    private final SampleValidator sampleValidator;
    private final TestResultValidator testValidator;
    private final Validator beanValidator;
    private final AnomalyClassifier anomalyClassifier;
    private final PollingAuditService auditService;

    public LabResultIngestionService(SampleRepository sampleRepository,
                                     SampleValidator sampleValidator,
                                     TestResultValidator testValidator,
                                     Validator beanValidator,
                                     AnomalyClassifier anomalyClassifier,
                                     PollingAuditService auditService) {
        this.sampleRepository = sampleRepository;
        this.sampleValidator = sampleValidator;
        this.testValidator = testValidator;
        this.beanValidator = beanValidator;
        this.anomalyClassifier = anomalyClassifier;
        this.auditService = auditService;
    }

    @Transactional
    public void ingest(List<SampleBatchDto> batch) {
        int validTests = 0, invalidTests = 0, duplicateTubes = 0;
        List<String> auditEvents = new ArrayList<>();

        // sampleIds already accepted in THIS batch, so a repeat tube inside one batch is caught
        // before it can hit the DB unique constraint.
        Set<String> seenInBatch = new HashSet<>();

        for (SampleBatchDto tube : batch) {
            Optional<String> structural = structuralViolation(tube);
            if (structural.isPresent()) {
                invalidTests += tube.tests() == null ? 0 : tube.tests().size();
                log.warn("Invalid tube [{}]: {}", tube.sampleId(), structural.get());
                auditEvents.add("Invalid tube " + tube.sampleId() + ": " + structural.get());
                continue;
            }
            Optional<String> tubeViolation = sampleValidator.findViolation(tube);
            if (tubeViolation.isPresent()) {
                invalidTests += tube.tests().size();
                log.warn("Rejected tube [{}]: {}", tube.sampleId(), tubeViolation.get());
                auditEvents.add("Rejected tube " + tube.sampleId() + ": " + tubeViolation.get());
                continue;
            }
            if (seenInBatch.contains(tube.sampleId()) || sampleRepository.existsBySampleId(tube.sampleId())) {
                duplicateTubes++;
                log.info("Duplicate tube skipped [{}]", tube.sampleId());
                auditEvents.add("Duplicate tube " + tube.sampleId());
                continue;
            }

            Sample sample = new Sample(tube.sampleId(), tube.patientId(), tube.measuredAt(), tube.deviceId());
            Set<String> seenTestCodes = new HashSet<>();

            for (TestResultDto test : tube.tests()) {
                // A test code repeated within one tube is a genuine duplicate; keep the first only.
                if (!seenTestCodes.add(test.testCode())) {
                    invalidTests++;
                    log.info("Duplicate test in tube [{}]: {}", tube.sampleId(), test.testCode());
                    auditEvents.add("Duplicate test " + tube.sampleId() + "/" + test.testCode());
                    continue;
                }
                Optional<String> testViolation = testValidator.findViolation(test);
                AnomalyStatus status;
                if (testViolation.isPresent()) {
                    status = AnomalyStatus.INVALID;
                    invalidTests++;
                    log.warn("Invalid test [{}/{}]: {}", tube.sampleId(), test.testCode(), testViolation.get());
                    auditEvents.add("Invalid test " + tube.sampleId() + "/" + test.testCode()
                            + ": " + testViolation.get());
                } else {
                    status = anomalyClassifier.classify(test.value(), test.referenceMin(), test.referenceMax());
                    validTests++;
                }
                sample.addTest(new LabResult(test.testCode(), test.testName(), test.value(),
                        test.unit(), test.referenceMin(), test.referenceMax(), status));
            }

            sampleRepository.save(sample);
            seenInBatch.add(tube.sampleId());
        }

        log.info("Ingest cycle: tubes={} validTests={} invalidTests={} duplicateTubes={}",
                batch.size(), validTests, invalidTests, duplicateTubes);

        auditService.recordProcessed(batch.size(), validTests, invalidTests, duplicateTubes, auditEvents);
    }

    private Optional<String> structuralViolation(SampleBatchDto tube) {
        Set<ConstraintViolation<SampleBatchDto>> violations = beanValidator.validate(tube);
        if (violations.isEmpty()) {
            return Optional.empty();
        }
        ConstraintViolation<SampleBatchDto> first = violations.iterator().next();
        return Optional.of("missing-field: " + first.getPropertyPath() + " " + first.getMessage());
    }
}
