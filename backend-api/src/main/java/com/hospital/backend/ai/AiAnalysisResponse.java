package com.hospital.backend.ai;

import java.time.Instant;
import java.util.List;

// API-facing view of an analysis. The JSON-array columns are parsed back into real lists here,
// so the client receives proper arrays, not strings.
public record AiAnalysisResponse(
        Long id,
        Long sampleFk,
        String model,
        String promptVersion,
        String summary,
        List<String> flaggedTests,
        List<String> suggestedFollowups,
        String disclaimer,
        Instant createdAt
) {
}
