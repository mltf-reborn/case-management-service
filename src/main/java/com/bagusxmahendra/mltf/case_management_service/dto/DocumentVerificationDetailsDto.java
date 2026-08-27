package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentVerificationDetailsDto {

    private String status;
    private String message;

    @JsonProperty("gcsUrl")
    @JsonAlias({"gcs_url", "documentUrl", "document_url", "fileUrl", "file_url"})
    private String gcsUrl;

    @JsonProperty("detectedDocumentType")
    @JsonAlias({"detected_document_type", "documentType", "document_type"})
    private String detectedDocumentType;

    private DocumentScoresDto scores;

    @JsonProperty("pixelLevelCheck")
    @JsonAlias({"pixel_level_check", "pixelCheck", "tamperCheck"})
    private PixelLevelCheckResultDto pixelLevelCheck;

    @JsonProperty("extractedFields")
    @JsonAlias({"extracted_fields", "fields"})
    private Map<String, Object> extractedFields;

    @JsonProperty("fieldDetails")
    @JsonAlias({"field_details"})
    private List<DocumentFieldDetailDto> fieldDetails;

    private ProcessingMetadataDto metadata;

    private Map<String, Object> additionalProperties = new HashMap<>();

    public DocumentVerificationDetailsDto() {
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

    public String getGcsUrl() {
        return gcsUrl;
    }

    public void setGcsUrl(String gcsUrl) {
        this.gcsUrl = gcsUrl;
    }

    public String getDetectedDocumentType() {
        return detectedDocumentType;
    }

    public void setDetectedDocumentType(String detectedDocumentType) {
        this.detectedDocumentType = detectedDocumentType;
    }

    public DocumentScoresDto getScores() {
        return scores;
    }

    public void setScores(DocumentScoresDto scores) {
        this.scores = scores;
    }

    public PixelLevelCheckResultDto getPixelLevelCheck() {
        return pixelLevelCheck;
    }

    public void setPixelLevelCheck(PixelLevelCheckResultDto pixelLevelCheck) {
        this.pixelLevelCheck = pixelLevelCheck;
    }

    public Map<String, Object> getExtractedFields() {
        return extractedFields;
    }

    public void setExtractedFields(Map<String, Object> extractedFields) {
        this.extractedFields = extractedFields;
    }

    public List<DocumentFieldDetailDto> getFieldDetails() {
        return fieldDetails;
    }

    public void setFieldDetails(List<DocumentFieldDetailDto> fieldDetails) {
        this.fieldDetails = fieldDetails;
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
