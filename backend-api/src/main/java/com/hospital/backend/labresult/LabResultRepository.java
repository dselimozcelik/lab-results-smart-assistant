package com.hospital.backend.labresult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LabResultRepository extends JpaRepository<LabResult, Long> {

    // Patient/measuredAt/deviceId now live on the parent Sample, so filters reach through r.sample.
    // Each filter is optional: a CAST tells PostgreSQL the bind parameter's type so it can
    // resolve "IS NULL" on an otherwise untyped parameter; the OR then skips absent filters.
    @Query("""
            SELECT r FROM LabResult r
            WHERE (CAST(:patientId AS string) IS NULL
                    OR LOWER(r.sample.patientId) = LOWER(CAST(:patientId AS string)))
              AND (CAST(:testCode AS string) IS NULL
                    OR LOWER(r.testCode) = LOWER(CAST(:testCode AS string)))
              AND (CAST(:status    AS string) IS NULL OR r.anomalyStatus = :status)
              AND (CAST(:from AS timestamp) IS NULL OR r.sample.measuredAt >= :from)
              AND (CAST(:to   AS timestamp) IS NULL OR r.sample.measuredAt <= :to)
            """)
    Page<LabResult> search(
            @Param("patientId") String patientId,
            @Param("testCode") String testCode,
            @Param("status") AnomalyStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT r.sample.patientId FROM LabResult r
            WHERE LOWER(r.sample.patientId) LIKE LOWER(CONCAT(:prefix, '%'))
            ORDER BY r.sample.patientId
            """)
    List<String> findPatientIdSuggestions(@Param("prefix") String prefix, Pageable pageable);
}
