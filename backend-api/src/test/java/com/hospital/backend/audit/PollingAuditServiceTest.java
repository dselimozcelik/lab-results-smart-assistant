package com.hospital.backend.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PollingAuditServiceTest {

    private final PollingAuditLogRepository repository = mock(PollingAuditLogRepository.class);
    private final PollingAuditService service = new PollingAuditService(repository, new ObjectMapper());

    @Test
    void processedCycleStoresStructuredDetails() {
        service.recordProcessed(2, 3, 1, 1, List.of("Invalid test S-1/GLU", "Duplicate tube S-2"));

        ArgumentCaptor<PollingAuditLog> captor = ArgumentCaptor.forClass(PollingAuditLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDetails())
                .contains("\"outcome\":\"PROCESSED\"")
                .contains("Invalid test S-1/GLU")
                .contains("Duplicate tube S-2");
    }

    @Test
    void failedCycleStoresFailureReason() {
        service.recordFailure(new RuntimeException("device unavailable"));

        ArgumentCaptor<PollingAuditLog> captor = ArgumentCaptor.forClass(PollingAuditLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDetails())
                .contains("\"outcome\":\"FAILED\"")
                .contains("device unavailable");
    }
}
