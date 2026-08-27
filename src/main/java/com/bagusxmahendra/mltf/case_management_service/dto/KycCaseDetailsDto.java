package com.bagusxmahendra.mltf.case_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KycCaseDetailsDto {

    @JsonProperty("userId")
    @JsonAlias({"user_id", "id"})
    private String userId;

    @JsonProperty("fullName")
    @JsonAlias({"full_name", "name", "applicantName", "applicant_name"})
    private String fullName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phoneNumber")
    @JsonAlias({"phone_number", "phone", "mobile"})
    private String phoneNumber;

    @JsonProperty("idCardNumber")
    @JsonAlias({"id_card_number", "idNumber", "id_number", "nationalId", "national_id"})
    private String idCardNumber;

    @JsonProperty("idCardType")
    @JsonAlias({"id_card_type", "idType", "id_type", "documentType", "document_type"})
    private String idCardType;

    @JsonProperty("dateOfBirth")
    @JsonAlias({"date_of_birth", "dob", "birthDate", "birth_date"})
    private String dateOfBirth;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    private String city;

    @JsonProperty("postalCode")
    @JsonAlias({"postal_code", "zipCode", "zip_code", "postcode"})
    private String postalCode;

    @JsonProperty("country")
    private String country;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("occupation")
    private String occupation;

    @JsonProperty("monthlyIncome")
    @JsonAlias({"monthly_income", "income"})
    private BigDecimal monthlyIncome;

    @JsonProperty("status")
    @JsonAlias({"kycStatus", "kyc_status", "decision"})
    private String status;

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
    @JsonAlias({"explanation", "notes", "summary"})
    private String remarks;

    @JsonProperty("verifiedBy")
    @JsonAlias({"verified_by", "reviewer"})
    private String verifiedBy;

    @JsonProperty("verifiedAt")
    @JsonAlias({"verified_at"})
    private String verifiedAt;

    @JsonProperty("referenceId")
    @JsonAlias({"reference_id", "sessionId", "session_id"})
    private String referenceId;

    @JsonProperty("externalKycSummary")
    @JsonAlias({"external_kyc_summary", "externalKyc", "external_kyc"})
    private Map<String, Object> externalKycSummary;

    private Map<String, Object> additionalProperties = new HashMap<>();

    public KycCaseDetailsDto() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getIdCardType() {
        return idCardType;
    }

    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(String verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Map<String, Object> getExternalKycSummary() {
        return externalKycSummary;
    }

    public void setExternalKycSummary(Map<String, Object> externalKycSummary) {
        this.externalKycSummary = externalKycSummary;
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
