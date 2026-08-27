package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentFieldDetailDto {

    private String key;
    private Object value;
    private double confidence;

    @JsonProperty("isSuspicious")
    @JsonAlias({"suspicious", "is_suspicious"})
    private boolean isSuspicious;

    private String notes;

    public DocumentFieldDetailDto() {
    }

    public DocumentFieldDetailDto(String key, Object value, double confidence, boolean isSuspicious, String notes) {
        this.key = key;
        this.value = value;
        this.confidence = confidence;
        this.isSuspicious = isSuspicious;
        this.notes = notes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public boolean isSuspicious() {
        return isSuspicious;
    }

    public void setSuspicious(boolean suspicious) {
        isSuspicious = suspicious;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
