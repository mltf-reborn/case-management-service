package com.bagusxmahendra.mltf.case_management_service.model;

public enum CaseStatus {
    IN_PROGRESS,
    ACCEPTED,
    REJECTED;

    /**
     * Maps this CaseStatus to the corresponding status string used in the kyc_profile table.
     * kyc_profile accepts: IN_REVIEW, SUCCESS, FAILED
     */
    public String toKycProfileStatus() {
        return switch (this) {
            case ACCEPTED  -> "SUCCESS";
            case REJECTED  -> "FAILED";
            default        -> "IN_REVIEW";
        };
    }

    public static CaseStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return IN_PROGRESS;
        }
        String upper = value.trim().toUpperCase();
        if ("ACCEPTED".equals(upper) || "APPROVED".equals(upper) || "APPROVE".equals(upper) || "SUCCESS".equals(upper) || "PASSED".equals(upper)) {
            return ACCEPTED;
        }
        if ("REJECTED".equals(upper) || "FAILED".equals(upper) || "REJECT".equals(upper) || "FAIL".equals(upper) || "FRAUD".equals(upper)) {
            return REJECTED;
        }
        if ("IN_PROGRESS".equals(upper) || "INPROGRESS".equals(upper) || "IN_REVIEW".equals(upper) || "INREVIEW".equals(upper) || "PENDING".equals(upper) || "REVIEW".equals(upper)) {
            return IN_PROGRESS;
        }
        try {
            return CaseStatus.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return IN_PROGRESS;
        }
    }
}
