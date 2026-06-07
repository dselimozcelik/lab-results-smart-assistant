package com.hospital.backend.labresult;

import com.hospital.backend.audit.PollingAuditService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LabResultPollerTest {

    private final DeviceClient deviceClient = mock(DeviceClient.class);
    private final LabResultIngestionService ingestionService = mock(LabResultIngestionService.class);
    private final PollingAuditService auditService = mock(PollingAuditService.class);
    private final LabResultPoller poller = new LabResultPoller(deviceClient, ingestionService, auditService);

    @Test
    void emptyBatchIsAudited() {
        when(deviceClient.fetchBatch()).thenReturn(List.of());

        poller.poll();

        verify(auditService).recordEmptyBatch();
        verify(ingestionService, never()).ingest(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deviceFailureIsAuditedAndDoesNotEscape() {
        RuntimeException failure = new RuntimeException("device unavailable");
        when(deviceClient.fetchBatch()).thenThrow(failure);

        poller.poll();

        verify(auditService).recordFailure(failure);
        verify(ingestionService, never()).ingest(org.mockito.ArgumentMatchers.any());
    }
}
