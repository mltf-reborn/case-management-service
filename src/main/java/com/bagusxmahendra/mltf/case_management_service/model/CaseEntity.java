package com.bagusxmahendra.mltf.case_management_service.model;

import com.google.cloud.Timestamp;
import com.google.cloud.spanner.Struct;

import java.time.Instant;

public record CaseEntity(
        String caseId,
        String userId,
        CaseType caseType,
        CaseStatus caseStatus,
        String documentUrl,
        String selfieUrl,
        String documentVerificationDetails,
        String selfieDetails,
        String kycDetails,
        String externalKycDetails,
        Double riskScore,
        String riskLevel,
        String rejectionReason,
        String remarks,
        String assignedTo,
        Instant createdAt,
        Instant updatedAt
) {

    public static CaseEntity fromStruct(Struct struct) {
        if (struct == null) {
            return null;
        }

        String caseId = struct.isNull("case_id") ? null : struct.getString("case_id");
        String userId = struct.isNull("user_id") ? null : struct.getString("user_id");
        
        String caseTypeStr = struct.isNull("case_type") ? null : struct.getString("case_type");
        CaseType caseType = CaseType.fromString(caseTypeStr);

        String caseStatusStr = struct.isNull("case_status") ? null : struct.getString("case_status");
        CaseStatus caseStatus = CaseStatus.fromString(caseStatusStr);

        String documentUrl = struct.isNull("document_url") ? null : struct.getString("document_url");
        String selfieUrl = struct.isNull("selfie_url") ? null : struct.getString("selfie_url");

        String docVerificationJson = null;
        if (!struct.isNull("document_verification_details")) {
            try {
                docVerificationJson = struct.getJson("document_verification_details");
            } catch (Exception e) {
                docVerificationJson = struct.getString("document_verification_details");
            }
        }

        String selfieDetailsJson = null;
        if (!struct.isNull("selfie_details")) {
            try {
                selfieDetailsJson = struct.getJson("selfie_details");
            } catch (Exception e) {
                selfieDetailsJson = struct.getString("selfie_details");
            }
        }

        String kycDetailsJson = null;
        if (!struct.isNull("kyc_details")) {
            try {
                kycDetailsJson = struct.getJson("kyc_details");
            } catch (Exception e) {
                kycDetailsJson = struct.getString("kyc_details");
            }
        }

        String externalKycDetailsJson = null;
        if (!struct.isNull("external_kyc_details")) {
            try {
                externalKycDetailsJson = struct.getJson("external_kyc_details");
            } catch (Exception e) {
                externalKycDetailsJson = struct.getString("external_kyc_details");
            }
        }

        Double riskScore = struct.isNull("risk_score") ? null : struct.getDouble("risk_score");
        String riskLevel = struct.isNull("risk_level") ? null : struct.getString("risk_level");
        String rejectionReason = struct.isNull("rejection_reason") ? null : struct.getString("rejection_reason");
        String remarks = struct.isNull("remarks") ? null : struct.getString("remarks");
        String assignedTo = struct.isNull("assigned_to") ? null : struct.getString("assigned_to");

        Instant createdAt = null;
        if (!struct.isNull("created_at")) {
            Timestamp ts = struct.getTimestamp("created_at");
            createdAt = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        }

        Instant updatedAt = null;
        if (!struct.isNull("updated_at")) {
            Timestamp ts = struct.getTimestamp("updated_at");
            updatedAt = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        }

        return new CaseEntity(
                caseId,
                userId,
                caseType,
                caseStatus,
                documentUrl,
                selfieUrl,
                docVerificationJson,
                selfieDetailsJson,
                kycDetailsJson,
                externalKycDetailsJson,
                riskScore,
                riskLevel,
                rejectionReason,
                remarks,
                assignedTo,
                createdAt,
                updatedAt
        );
    }
}
