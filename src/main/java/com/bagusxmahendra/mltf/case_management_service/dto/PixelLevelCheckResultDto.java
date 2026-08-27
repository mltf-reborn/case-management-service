package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PixelLevelCheckResultDto {

    @JsonProperty("isTampered")
    @JsonAlias({"tampered", "is_tampered"})
    private boolean isTampered;

    @JsonProperty("tamperingRiskLevel")
    @JsonAlias({"tampering_risk_level", "riskLevel", "risk_level"})
    private String tamperingRiskLevel;

    @JsonProperty("tamperingConfidence")
    @JsonAlias({"tampering_confidence"})
    private double tamperingConfidence;

    @JsonProperty("findings")
    private String findings;

    @JsonProperty("anomalies")
    private List<PixelAnomalyDto> anomalies = new ArrayList<>();

    public PixelLevelCheckResultDto() {
    }

    public PixelLevelCheckResultDto(boolean isTampered, String tamperingRiskLevel, double tamperingConfidence, String findings, List<PixelAnomalyDto> anomalies) {
        this.isTampered = isTampered;
        this.tamperingRiskLevel = tamperingRiskLevel;
        this.tamperingConfidence = tamperingConfidence;
        this.findings = findings;
        this.anomalies = anomalies != null ? anomalies : new ArrayList<>();
    }

    public boolean isTampered() {
        return isTampered;
    }

    public void setTampered(boolean tampered) {
        isTampered = tampered;
    }

    public String getTamperingRiskLevel() {
        return tamperingRiskLevel;
    }

    public void setTamperingRiskLevel(String tamperingRiskLevel) {
        this.tamperingRiskLevel = tamperingRiskLevel;
    }

    public double getTamperingConfidence() {
        return tamperingConfidence;
    }

    public void setTamperingConfidence(double tamperingConfidence) {
        this.tamperingConfidence = tamperingConfidence;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public List<PixelAnomalyDto> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<PixelAnomalyDto> anomalies) {
        this.anomalies = anomalies;
    }
}
