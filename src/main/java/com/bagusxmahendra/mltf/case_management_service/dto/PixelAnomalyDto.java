package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PixelAnomalyDto {

    @JsonProperty("targetField")
    @JsonAlias({"target_field", "field"})
    private String targetField;

    @JsonProperty("anomalyType")
    @JsonAlias({"anomaly_type", "type"})
    private String anomalyType;

    private String severity;
    private String description;
    private String location;

    public PixelAnomalyDto() {
    }

    public PixelAnomalyDto(String targetField, String anomalyType, String severity, String description, String location) {
        this.targetField = targetField;
        this.anomalyType = anomalyType;
        this.severity = severity;
        this.description = description;
        this.location = location;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
