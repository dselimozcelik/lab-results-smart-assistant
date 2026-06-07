package com.hospital.backend.labresult;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SampleRepository extends JpaRepository<Sample, Long> {

    boolean existsBySampleId(String sampleId);

    Optional<Sample> findBySampleId(String sampleId);
}
