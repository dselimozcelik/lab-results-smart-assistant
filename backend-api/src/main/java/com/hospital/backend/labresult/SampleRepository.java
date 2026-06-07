package com.hospital.backend.labresult;

import com.hospital.backend.patient.PatientSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SampleRepository extends JpaRepository<Sample, Long> {

    boolean existsBySampleId(String sampleId);

    // Eager-load the panel so the detail response can read tests without a lazy-load surprise.
    @Query("SELECT s FROM Sample s LEFT JOIN FETCH s.tests WHERE s.patientId = :patientId ORDER BY s.measuredAt DESC")
    List<Sample> findByPatientIdWithTests(@Param("patientId") String patientId);

    // Level-1 rollup: one row per patient. worstSeverity ranks the most severe status across the
    // patient's tests in SQL (CRITICAL=4 > HIGH/LOW=3 > INVALID=2 > NORMAL=1); the service maps it
    // back to an AnomalyStatus. Grouping over the test rows joined to their sample.
    @Query("""
            SELECT s.patientId AS patientId,
                   COUNT(r) AS testCount,
                   COUNT(DISTINCT s.id) AS sampleCount,
                   MAX(CASE r.anomalyStatus
                         WHEN com.hospital.backend.labresult.AnomalyStatus.CRITICAL THEN 4
                         WHEN com.hospital.backend.labresult.AnomalyStatus.HIGH THEN 3
                         WHEN com.hospital.backend.labresult.AnomalyStatus.LOW THEN 3
                         WHEN com.hospital.backend.labresult.AnomalyStatus.INVALID THEN 2
                         ELSE 1 END) AS worstSeverity,
                   MAX(s.measuredAt) AS lastMeasuredAt
            FROM LabResult r JOIN r.sample s
            GROUP BY s.patientId
            ORDER BY MAX(s.measuredAt) DESC
            """)
    Page<PatientSummaryRow> findPatientSummaries(Pageable pageable);

    Optional<Sample> findBySampleId(String sampleId);
}
