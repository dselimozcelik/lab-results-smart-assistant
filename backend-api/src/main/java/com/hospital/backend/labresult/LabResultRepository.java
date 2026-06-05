package com.hospital.backend.labresult;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LabResultRepository extends JpaRepository<LabResult, Long> {

    boolean existsBySampleId(String sampleId);
}
