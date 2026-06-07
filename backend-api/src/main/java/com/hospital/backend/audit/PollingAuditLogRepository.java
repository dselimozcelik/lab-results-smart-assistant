package com.hospital.backend.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PollingAuditLogRepository extends JpaRepository<PollingAuditLog, Long> {
}
