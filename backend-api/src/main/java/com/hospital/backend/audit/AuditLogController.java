package com.hospital.backend.audit;

import com.hospital.backend.common.PageResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final PollingAuditLogRepository repository;

    public AuditLogController(PollingAuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public PageResponse<PollingAuditLogResponse> list(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(PollingAuditLogResponse::from));
    }
}
