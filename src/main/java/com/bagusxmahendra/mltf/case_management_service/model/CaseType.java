package com.bagusxmahendra.mltf.case_management_service.model;

public enum CaseType {
    KYC,
    LOAN_APPLICATION;

    public static CaseType fromString(String value) {
        if (value == null || value.isBlank()) {
            return KYC;
        }
        String upper = value.trim().toUpperCase();
        if ("LOAN_APPLICATION".equals(upper) || "LOAN".equals(upper) || "LOANAPPLICATION".equals(upper) || "LOAN_APP".equals(upper)) {
            return LOAN_APPLICATION;
        }
        if ("KYC".equals(upper)) {
            return KYC;
        }
        try {
            return CaseType.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return KYC;
        }
    }
}
