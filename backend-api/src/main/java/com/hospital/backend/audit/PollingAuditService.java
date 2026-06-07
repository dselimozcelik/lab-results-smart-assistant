package com.hospital.backend.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// Centralises polling audit writes so successful, empty and failed cycles share one format.
@Service
public class PollingAuditService {

    private static final Logger log = LoggerFactory.getLogger(PollingAuditService.class);

    private final PollingAuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public PollingAuditService(PollingAuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordProcessed(int fetched, int valid, int invalid, int duplicate, List<String> events) {
        save(fetched, valid, invalid, duplicate, "PROCESSED", events);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEmptyBatch() {
        save(0, 0, 0, 0, "EMPTY_BATCH", List.of("Device returned no samples"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Exception exception) {
        String reason = exception.getMessage() == null
                ? exception.getClass().getSimpleName()
                : exception.getClass().getSimpleName() + ": " + exception.getMessage();
        save(0, 0, 0, 0, "FAILED", List.of(reason));
    }

    private void save(int fetched, int valid, int invalid, int duplicate, String outcome, List<String> events) {
        repository.save(new PollingAuditLog(
                fetched, valid, invalid, duplicate, details(outcome, events)));
    }

    private String details(String outcome, List<String> events) {
        try {
            return objectMapper.writeValueAsString(Map.of("outcome", outcome, "events", events));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialise polling audit details: {}", e.getMessage());
            return "{\"outcome\":\"" + outcome + "\",\"events\":[\"details unavailable\"]}";
        }
    }
}
