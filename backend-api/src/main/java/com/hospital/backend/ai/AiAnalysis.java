package com.hospital.backend.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// One cached LLM analysis for a lab result. Maps 1:1 to the ai_analysis table (Flyway-owned).
// flaggedTests / suggestedFollowups hold a JSON array string (parsed to a list at the API edge).
@Entity
@Table(name = "ai_analysis")
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lab_result_id", nullable = false)
    private Long labResultId;

    @Column(nullable = false)
    private String model;

    @Column(name = "prompt_version", nullable = false)
    private String promptVersion;

    @Column(nullable = false)
    private String summary;

    @Column(name = "flagged_tests", nullable = false)
    private String flaggedTests;

    @Column(name = "suggested_followups", nullable = false)
    private String suggestedFollowups;

    @Column(nullable = false)
    private String disclaimer;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected AiAnalysis() {
        // JPA
    }

    public AiAnalysis(Long labResultId, String model, String promptVersion, String summary,
                      String flaggedTests, String suggestedFollowups, String disclaimer) {
        this.labResultId = labResultId;
        this.model = model;
        this.promptVersion = promptVersion;
        this.summary = summary;
        this.flaggedTests = flaggedTests;
        this.suggestedFollowups = suggestedFollowups;
        this.disclaimer = disclaimer;
    }

    public Long getId() {
        return id;
    }

    public Long getLabResultId() {
        return labResultId;
    }

    public String getModel() {
        return model;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public String getSummary() {
        return summary;
    }

    public String getFlaggedTests() {
        return flaggedTests;
    }

    public String getSuggestedFollowups() {
        return suggestedFollowups;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
