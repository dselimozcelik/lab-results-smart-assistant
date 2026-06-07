package com.hospital.backend.labresult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface LabResultRepository extends JpaRepository<LabResult, Long> {

    boolean existsBySampleId(String sampleId);

    // Each filter is optional: ":param IS NULL OR ..." skips the condition when the param is absent.
    @Query("""
            SELECT r FROM LabResult r
            WHERE (:patientId IS NULL OR r.patientId = :patientId)
              AND (:testCode  IS NULL OR r.testCode  = :testCode)
              AND (:status    IS NULL OR r.anomalyStatus = :status)
              AND (:from      IS NULL OR r.measuredAt >= :from)
              AND (:to        IS NULL OR r.measuredAt <= :to)
            """)
    Page<LabResult> search(
            @Param("patientId") String patientId,
            @Param("testCode") String testCode,
            @Param("status") AnomalyStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
