package com.bagusxmahendra.mltf.case_management_service.model;

public enum CaseType {
    KYC;

    public static CaseType fromString(String value) {
        if (value == null || value.isBlank()) {
            return KYC;
        }
        try {
            return CaseType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return KYC;
        }
    }
}
