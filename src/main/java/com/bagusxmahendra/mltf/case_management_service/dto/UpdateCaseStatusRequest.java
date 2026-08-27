package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCaseStatusRequest {

    @NotBlank(message = "caseStatus is required (IN_PROGRESS, ACCEPTED, REJECTED)")
    @JsonProperty("caseStatus")
    @JsonAlias({"case_status", "status"})
    private String caseStatus;

    @JsonProperty("remarks")
    @JsonAlias({"notes", "comment", "explanation"})
    private String remarks;

    @JsonProperty("rejectionReason")
    @JsonAlias({"rejection_reason", "rejectReason", "reject_reason"})
    private String rejectionReason;

    @JsonProperty("assignedTo")
    @JsonAlias({"assigned_to", "reviewer", "verifiedBy", "verified_by"})
    private String assignedTo;

    public UpdateCaseStatusRequest() {
    }

    public UpdateCaseStatusRequest(String caseStatus, String remarks, String rejectionReason, String assignedTo) {
        this.caseStatus = caseStatus;
        this.remarks = remarks;
        this.rejectionReason = rejectionReason;
        this.assignedTo = assignedTo;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
