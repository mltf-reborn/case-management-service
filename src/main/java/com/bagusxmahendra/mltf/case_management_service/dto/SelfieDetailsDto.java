package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfieDetailsDto {

    private String status;
    private String message;

    @JsonProperty("isIdentical")
    @JsonAlias({"is_identical", "identical", "isMatch", "is_match", "matched"})
    private Boolean isIdentical;

    @JsonProperty("confidenceScore")
    @JsonAlias({"confidentScore", "confident_score", "confidence_score", "confidence", "score", "matchScore"})
    private Double confidenceScore;

    @JsonProperty("matchStatus")
    @JsonAlias({"match_status", "statusMatch", "verdict"})
    private String matchStatus;

    @JsonProperty("explanation")
    @JsonAlias({"explaination", "description", "reasoning", "details"})
    private String explanation;

    @JsonProperty("idDocumentUrl")
    @JsonAlias({"id_document_url", "idDocUrl", "idGcsUrl"})
    private String idDocumentUrl;

    @JsonProperty("selfieUrl")
    @JsonAlias({"selfie_url", "selfieGcsUrl", "selfie_gcs_url"})
    private String selfieUrl;

    @JsonProperty("facialComparisonDetails")
    @JsonAlias({"facial_comparison_details", "comparisonDetails", "facialComparison"})
    private FacialComparisonDetailsDto facialComparisonDetails;

    private ProcessingMetadataDto metadata;

    private Map<String, Object> additionalProperties = new HashMap<>();

    public SelfieDetailsDto() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsIdentical() {
        return isIdentical;
    }

    public void setIsIdentical(Boolean identical) {
        isIdentical = identical;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getIdDocumentUrl() {
        return idDocumentUrl;
    }

    public void setIdDocumentUrl(String idDocumentUrl) {
        this.idDocumentUrl = idDocumentUrl;
    }

    public String getSelfieUrl() {
        return selfieUrl;
    }

    public void setSelfieUrl(String selfieUrl) {
        this.selfieUrl = selfieUrl;
    }

    public FacialComparisonDetailsDto getFacialComparisonDetails() {
        return facialComparisonDetails;
    }

    public void setFacialComparisonDetails(FacialComparisonDetailsDto facialComparisonDetails) {
        this.facialComparisonDetails = facialComparisonDetails;
    }

    public ProcessingMetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(ProcessingMetadataDto metadata) {
        this.metadata = metadata;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
