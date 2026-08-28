package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCaseRequest {

    @JsonProperty("caseId")
    @JsonAlias({"case_id", "id", "referenceId", "reference_id"})
    private String caseId;

    @NotBlank(message = "userId is required")
    @JsonProperty("userId")
    @JsonAlias({"user_id", "applicantId", "applicant_id"})
    private String userId;

    @JsonProperty("caseType")
    @JsonAlias({"case_type", "type"})
    private String caseType;

    @JsonProperty("caseStatus")
    @JsonAlias({"case_status", "status"})
    private String caseStatus;

    @JsonProperty("documentUrl")
    @JsonAlias({"document_url", "idDocumentUrl", "id_document_url", "documentGcsUrl", "idGcsUrl", "id_gcs_url", "docUrl", "doc_url"})
    private String documentUrl;

    @JsonProperty("selfieUrl")
    @JsonAlias({"selfie_url", "selfieGcsUrl", "selfie_gcs_url"})
    private String selfieUrl;

    @JsonProperty("kycDetails")
    @JsonAlias({"kyc_details", "kycCaseDetails", "kyc_case_details", "kycProfile", "kyc_profile", "profile"})
    private Object kycDetails;

    @JsonProperty("externalKycDetails")
    @JsonAlias({"external_kyc_details", "externalKyc", "extKycDetails", "ext_kyc_details"})
    private Object externalKycDetails;

    @JsonProperty("documentVerificationDetails")
    @JsonAlias({"document_verification_details", "docVerificationDetails", "doc_verification_details", "documentProcessingResult", "docProcessingResponse", "documentVerification"})
    private Object documentVerificationDetails;

    @JsonProperty("selfieDetails")
    @JsonAlias({"selfie_details", "selfieValidationDetails", "selfie_validation_details", "selfieValidationResult", "selfieResponse", "selfieValidation"})
    private Object selfieDetails;

    @JsonProperty("riskScore")
    @JsonAlias({"risk_score", "score"})
    private Double riskScore;

    @JsonProperty("riskLevel")
    @JsonAlias({"risk_level"})
    private String riskLevel;

    @JsonProperty("rejectionReason")
    @JsonAlias({"rejection_reason", "rejectReason", "reject_reason"})
    private String rejectionReason;

    @JsonProperty("remarks")
    @JsonAlias({"explanation", "notes", "summary", "decisionRationale"})
    private String remarks;

    @JsonProperty("assignedTo")
    @JsonAlias({"assigned_to", "verifiedBy", "verified_by", "reviewer"})
    private String assignedTo;

    public CreateCaseRequest() {
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getSelfieUrl() {
        return selfieUrl;
    }

    public void setSelfieUrl(String selfieUrl) {
        this.selfieUrl = selfieUrl;
    }

    public Object getKycDetails() {
        return kycDetails;
    }

    public void setKycDetails(Object kycDetails) {
        this.kycDetails = kycDetails;
    }

    public Object getExternalKycDetails() {
        return externalKycDetails;
    }

    public void setExternalKycDetails(Object externalKycDetails) {
        this.externalKycDetails = externalKycDetails;
    }

    public Object getDocumentVerificationDetails() {
        return documentVerificationDetails;
    }

    public void setDocumentVerificationDetails(Object documentVerificationDetails) {
        this.documentVerificationDetails = documentVerificationDetails;
    }

    public Object getSelfieDetails() {
        return selfieDetails;
    }

    public void setSelfieDetails(Object selfieDetails) {
        this.selfieDetails = selfieDetails;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
