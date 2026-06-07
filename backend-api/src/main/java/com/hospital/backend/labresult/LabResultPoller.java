package com.hospital.backend.labresult;

import com.hospital.backend.audit.PollingAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// Periodically pulls a batch from the device and hands it to ingestion.
// fixedDelay so a slow cycle never overlaps the next; failures are logged, not fatal.
@Component
public class LabResultPoller {

    private static final Logger log = LoggerFactory.getLogger(LabResultPoller.class);

    private final DeviceClient deviceClient;
    private final LabResultIngestionService ingestionService;
    private final PollingAuditService auditService;

    public LabResultPoller(DeviceClient deviceClient,
                           LabResultIngestionService ingestionService,
                           PollingAuditService auditService) {
        this.deviceClient = deviceClient;
        this.ingestionService = ingestionService;
        this.auditService = auditService;
    }

    @Scheduled(fixedDelayString = "${lab.polling.fixed-delay-ms}")
    public void poll() {
        try {
            List<SampleBatchDto> batch = deviceClient.fetchBatch();
            if (batch == null || batch.isEmpty()) {
                auditService.recordEmptyBatch();
                return;
            }
            ingestionService.ingest(batch);
        } catch (Exception e) {
            // Device offline or transient error: log and retry next cycle. Never crash.
            log.warn("Polling cycle failed, will retry: {}", e.getMessage());
            auditService.recordFailure(e);
        }
    }
}
