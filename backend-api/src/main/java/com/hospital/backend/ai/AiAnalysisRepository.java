package com.hospital.backend.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {

    // Cache lookup: an analysis already exists for this result + model + prompt version.
    Optional<AiAnalysis> findByLabResultIdAndModelAndPromptVersion(
            Long labResultId, String model, String promptVersion);
}
