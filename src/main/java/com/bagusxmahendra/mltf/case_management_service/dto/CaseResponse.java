package com.bagusxmahendra.mltf.case_management_service.dto;

import com.bagusxmahendra.mltf.case_management_service.model.CaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CaseResponse(
        String caseId,
        String userId,
        String caseType,
        String caseStatus,
        String documentUrl,
        String selfieUrl,
        Object documentVerificationDetails,
        Object selfieDetails,
        Object kycDetails,
        Double riskScore,
        String riskLevel,
        String rejectionReason,
        String remarks,
        String assignedTo,
        Instant createdAt,
        Instant updatedAt
) {

    public static CaseResponse from(CaseEntity entity, ObjectMapper objectMapper) {
        if (entity == null) {
            return null;
        }

        Object docVerification = parseJson(entity.documentVerificationDetails(), objectMapper);
        Object selfieDetails = parseJson(entity.selfieDetails(), objectMapper);
        Object kycDetails = parseJson(entity.kycDetails(), objectMapper);

        return new CaseResponse(
                entity.caseId(),
                entity.userId(),
                entity.caseType() != null ? entity.caseType().name() : "KYC",
                entity.caseStatus() != null ? entity.caseStatus().name() : "IN_PROGRESS",
                entity.documentUrl(),
                entity.selfieUrl(),
                docVerification,
                selfieDetails,
                kycDetails,
                entity.riskScore(),
                entity.riskLevel(),
                entity.rejectionReason(),
                entity.remarks(),
                entity.assignedTo(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

    private static Object parseJson(String jsonStr, ObjectMapper objectMapper) {
        if (jsonStr == null || jsonStr.isBlank()) {
            return null;
        }
        try {
            if (jsonStr.trim().startsWith("{")) {
                return objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            } else if (jsonStr.trim().startsWith("[")) {
                return objectMapper.readValue(jsonStr, new TypeReference<Object>() {});
            }
            return jsonStr;
        } catch (Exception e) {
            return jsonStr;
        }
    }
}
