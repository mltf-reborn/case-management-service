package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentScoresDto {

    @JsonProperty("documentScore")
    @JsonAlias({"document_score", "overallScore", "score"})
    private double documentScore;

    @JsonProperty("originalityScore")
    @JsonAlias({"originality_score"})
    private double originalityScore;

    @JsonProperty("confidenceScore")
    @JsonAlias({"confidence_score", "confidentScore"})
    private double confidenceScore;

    @JsonProperty("scoringBreakdown")
    @JsonAlias({"scoring_breakdown", "breakdown"})
    private String scoringBreakdown;

    public DocumentScoresDto() {
    }

    public DocumentScoresDto(double documentScore, double originalityScore, double confidenceScore, String scoringBreakdown) {
        this.documentScore = documentScore;
        this.originalityScore = originalityScore;
        this.confidenceScore = confidenceScore;
        this.scoringBreakdown = scoringBreakdown;
    }

    public double getDocumentScore() {
        return documentScore;
    }

    public void setDocumentScore(double documentScore) {
        this.documentScore = documentScore;
    }

    public double getOriginalityScore() {
        return originalityScore;
    }

    public void setOriginalityScore(double originalityScore) {
        this.originalityScore = originalityScore;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getScoringBreakdown() {
        return scoringBreakdown;
    }

    public void setScoringBreakdown(String scoringBreakdown) {
        this.scoringBreakdown = scoringBreakdown;
    }
}
