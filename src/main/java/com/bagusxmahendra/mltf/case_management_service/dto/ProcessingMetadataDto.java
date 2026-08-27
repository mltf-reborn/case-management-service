package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessingMetadataDto {

    private String model;

    @JsonProperty("agentFramework")
    @JsonAlias({"agent_framework"})
    private String agentFramework;

    @JsonProperty("detectedMimeType")
    @JsonAlias({"detected_mime_type", "mimeType", "mime_type"})
    private String detectedMimeType;

    @JsonProperty("processedAt")
    @JsonAlias({"processed_at"})
    private Instant processedAt;

    @JsonProperty("executionDurationMs")
    @JsonAlias({"execution_duration_ms", "durationMs", "duration_ms"})
    private Long executionDurationMs;

    public ProcessingMetadataDto() {
    }

    public ProcessingMetadataDto(String model, String agentFramework, String detectedMimeType, Instant processedAt, Long executionDurationMs) {
        this.model = model;
        this.agentFramework = agentFramework;
        this.detectedMimeType = detectedMimeType;
        this.processedAt = processedAt;
        this.executionDurationMs = executionDurationMs;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAgentFramework() {
        return agentFramework;
    }

    public void setAgentFramework(String agentFramework) {
        this.agentFramework = agentFramework;
    }

    public String getDetectedMimeType() {
        return detectedMimeType;
    }

    public void setDetectedMimeType(String detectedMimeType) {
        this.detectedMimeType = detectedMimeType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(Long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }
}
