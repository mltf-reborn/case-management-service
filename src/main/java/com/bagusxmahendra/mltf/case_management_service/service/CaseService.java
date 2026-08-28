package com.bagusxmahendra.mltf.case_management_service.service;

import com.bagusxmahendra.mltf.case_management_service.dto.CaseResponse;
import com.bagusxmahendra.mltf.case_management_service.dto.CreateCaseRequest;
import com.bagusxmahendra.mltf.case_management_service.dto.UpdateCaseStatusRequest;
import com.bagusxmahendra.mltf.case_management_service.model.CaseEntity;
import com.bagusxmahendra.mltf.case_management_service.model.CaseStatus;
import com.bagusxmahendra.mltf.case_management_service.model.CaseType;
import com.bagusxmahendra.mltf.case_management_service.repository.CaseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    private final CaseRepository caseRepository;
    private final ObjectMapper objectMapper;

    public CaseService(CaseRepository caseRepository, ObjectMapper objectMapper) {
        this.caseRepository = caseRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new case (case_type=KYC, case_status=IN_PROGRESS by default),
     * serializes all KYC, document verification, and selfie details to JSON format,
     * and persists the record to Google Cloud Spanner.
     *
     * @param request the case creation request payload
     * @return Mono of CaseResponse
     */
    public Mono<CaseResponse> createCase(CreateCaseRequest request) {
        if (request == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required"));
        }

        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field 'userId' is required"));
        }

        String userId = request.getUserId().trim();

        // 1. Generate or sanitize case ID
        String caseId = (request.getCaseId() != null && !request.getCaseId().isBlank())
                ? request.getCaseId().trim()
                : generateCaseId();

        // 2. Serialize Details to JSON Strings for Spanner JSON column storage
        String docVerificationJson = convertToJsonString(request.getDocumentVerificationDetails());
        String selfieDetailsJson = convertToJsonString(request.getSelfieDetails());
        String kycDetailsJson = convertToJsonString(request.getKycDetails());
        String externalKycDetailsJson = convertToJsonString(request.getExternalKycDetails());

        // 3. Enforce Business Rule: Case creation is ONLY for IN_REVIEW status.
        // Automated APPROVED and REJECTED statuses are processed automatically and do not create cases.
        String incomingKycStatus = extractJsonField(kycDetailsJson, "status", "kycStatus", "decision");
        if (incomingKycStatus != null) {
            String upperStatus = incomingKycStatus.trim().toUpperCase();
            if ("APPROVED".equals(upperStatus) || "SUCCESS".equals(upperStatus) || "ACCEPTED".equals(upperStatus)) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Case creation is only applicable for KYC applications with IN_REVIEW status. APPROVED applications are processed automatically and do not require case creation."
                ));
            }
            if ("REJECTED".equals(upperStatus) || "FAILED".equals(upperStatus) || "FRAUD".equals(upperStatus)) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Case creation is only applicable for KYC applications with IN_REVIEW status. REJECTED applications are processed automatically and do not require case creation."
                ));
            }
        }

        if (request.getCaseStatus() != null) {
            String upperCaseStatus = request.getCaseStatus().trim().toUpperCase();
            if ("ACCEPTED".equals(upperCaseStatus) || "APPROVED".equals(upperCaseStatus) || "REJECTED".equals(upperCaseStatus) || "FAILED".equals(upperCaseStatus)) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "New cases can only be created with IN_PROGRESS status for IN_REVIEW KYC submissions. Use PATCH /api/v1/case/{caseId}/status to update status to ACCEPTED or REJECTED after compliance review."
                ));
            }
        }

        CaseType caseType = request.getCaseType() != null
                ? CaseType.fromString(request.getCaseType())
                : CaseType.KYC;

        CaseStatus caseStatus = CaseStatus.IN_PROGRESS;

        // 4. Resolve 2 GCS URLs (Document and Selfie) from root or extracted from details
        String documentUrl = resolveDocumentUrl(request.getDocumentUrl(), docVerificationJson, selfieDetailsJson, kycDetailsJson);
        String selfieUrl = resolveSelfieUrl(request.getSelfieUrl(), selfieDetailsJson, docVerificationJson, kycDetailsJson);

        // 5. Resolve Risk, Remarks, and Rejection fields with fallbacks
        Double riskScore = resolveRiskScore(request.getRiskScore(), kycDetailsJson, docVerificationJson, selfieDetailsJson);
        String riskLevel = resolveRiskLevel(request.getRiskLevel(), kycDetailsJson, docVerificationJson, selfieDetailsJson);
        String rejectionReason = resolveRejectionReason(request.getRejectionReason(), kycDetailsJson, docVerificationJson, selfieDetailsJson);
        String remarks = resolveRemarks(request.getRemarks(), kycDetailsJson, docVerificationJson, selfieDetailsJson);
        String assignedTo = request.getAssignedTo() != null ? request.getAssignedTo().trim() : "supervisor-agent";

        Instant now = Instant.now();

        CaseEntity entity = new CaseEntity(
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
                now,
                now
        );

        log.info("Persisting new case to Spanner – caseId: {}, userId: {}, type: {}, status: {}, docUrl: {}, selfieUrl: {}",
                caseId, userId, caseType, caseStatus, documentUrl, selfieUrl);

        return caseRepository.save(entity)
                .thenReturn(CaseResponse.from(entity, objectMapper));
    }

    /**
     * Retrieve a case by its unique ID.
     */
    public Mono<CaseResponse> getCaseById(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Case ID is required"));
        }

        String sanitizedId = caseId.trim();
        log.info("Fetching case by ID: {}", sanitizedId);

        return caseRepository.findById(sanitizedId)
                .map(entity -> CaseResponse.from(entity, objectMapper))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Case not found with ID: " + sanitizedId
                )));
    }

    /**
     * Retrieve all cases for a given user ID.
     */
    public Flux<CaseResponse> getCasesByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required"));
        }

        String sanitizedUserId = userId.trim();
        log.info("Fetching cases for userId: {}", sanitizedUserId);

        return caseRepository.findByUserId(sanitizedUserId)
                .map(entity -> CaseResponse.from(entity, objectMapper));
    }

    /**
     * Retrieve cases by case status.
     */
    public Flux<CaseResponse> getCasesByStatus(CaseStatus status) {
        if (status == null) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Case status is required"));
        }

        log.info("Fetching cases by status: {}", status);
        return caseRepository.findByStatus(status)
                .map(entity -> CaseResponse.from(entity, objectMapper));
    }

    /**
     * Retrieve all cases.
     */
    public Flux<CaseResponse> getAllCases() {
        log.info("Fetching all cases");
        return caseRepository.findAll()
                .map(entity -> CaseResponse.from(entity, objectMapper));
    }

    /**
     * Update case status and notes.
     */
    public Mono<CaseResponse> updateCaseStatus(String caseId, UpdateCaseStatusRequest updateRequest) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Case ID is required"));
        }
        if (updateRequest == null || updateRequest.getCaseStatus() == null || updateRequest.getCaseStatus().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field 'caseStatus' is required (IN_PROGRESS, ACCEPTED, REJECTED)"));
        }

        String sanitizedId = caseId.trim();
        CaseStatus newStatus = CaseStatus.fromString(updateRequest.getCaseStatus());

        return caseRepository.findById(sanitizedId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Case not found with ID: " + sanitizedId
                )))
                .flatMap(existing -> {
                    String updatedRemarks = updateRequest.getRemarks() != null
                            ? updateRequest.getRemarks().trim()
                            : existing.remarks();

                    String updatedRejection = updateRequest.getRejectionReason() != null
                            ? updateRequest.getRejectionReason().trim()
                            : existing.rejectionReason();

                    String updatedAssignedTo = updateRequest.getAssignedTo() != null
                            ? updateRequest.getAssignedTo().trim()
                            : existing.assignedTo();

                    Instant now = Instant.now();

                    CaseEntity updatedEntity = new CaseEntity(
                            existing.caseId(),
                            existing.userId(),
                            existing.caseType(),
                            newStatus,
                            existing.documentUrl(),
                            existing.selfieUrl(),
                            existing.documentVerificationDetails(),
                            existing.selfieDetails(),
                            existing.kycDetails(),
                            existing.externalKycDetails(),
                            existing.riskScore(),
                            existing.riskLevel(),
                            updatedRejection,
                            updatedRemarks,
                            updatedAssignedTo,
                            existing.createdAt(),
                            now
                    );

                    log.info("Updating case status – caseId: {}, userId: {}, oldStatus: {}, newStatus: {}",
                            sanitizedId, existing.userId(), existing.caseStatus(), newStatus);

                    return caseRepository.save(updatedEntity)
                            .then(caseRepository.updateKycProfileStatus(
                                    existing.userId(),
                                    newStatus.toKycProfileStatus(),
                                    updatedRemarks,
                                    updatedRejection,
                                    updatedAssignedTo
                            ))
                            .thenReturn(CaseResponse.from(updatedEntity, objectMapper));
                });
    }

    private String generateCaseId() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "CASE-KYC-" + System.currentTimeMillis() % 1000000 + "-" + suffix;
    }

    private String convertToJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String s) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) return null;
            return trimmed;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return String.valueOf(obj);
        }
    }

    private String resolveDocumentUrl(String rootUrl, String docJson, String selfieJson, String kycJson) {
        if (rootUrl != null && !rootUrl.isBlank()) {
            return rootUrl.trim();
        }
        String found = extractJsonField(docJson, "gcsUrl", "documentUrl", "idDocumentUrl", "fileUrl");
        if (found != null) return found;
        found = extractJsonField(selfieJson, "idDocumentUrl", "documentUrl");
        if (found != null) return found;
        return extractJsonField(kycJson, "documentUrl", "idDocumentUrl");
    }

    private String resolveSelfieUrl(String rootUrl, String selfieJson, String docJson, String kycJson) {
        if (rootUrl != null && !rootUrl.isBlank()) {
            return rootUrl.trim();
        }
        String found = extractJsonField(selfieJson, "selfieUrl", "gcsUrl", "fileUrl");
        if (found != null) return found;
        found = extractJsonField(docJson, "selfieUrl");
        if (found != null) return found;
        return extractJsonField(kycJson, "selfieUrl");
    }

    private Double resolveRiskScore(Double rootRiskScore, String kycJson, String docJson, String selfieJson) {
        if (rootRiskScore != null) return rootRiskScore;
        Double score = extractJsonDouble(kycJson, "riskScore", "score");
        if (score != null) return score;
        score = extractJsonDouble(selfieJson, "confidenceScore", "confidentScore");
        if (score != null) return score;
        return extractJsonDouble(docJson, "scores.documentScore", "documentScore");
    }

    private String resolveRiskLevel(String rootRiskLevel, String kycJson, String docJson, String selfieJson) {
        if (rootRiskLevel != null && !rootRiskLevel.isBlank()) return rootRiskLevel.trim();
        String level = extractJsonField(kycJson, "riskLevel");
        if (level != null) return level;
        level = extractJsonField(docJson, "pixelLevelCheck.tamperingRiskLevel", "tamperingRiskLevel");
        if (level != null) return level;
        return extractJsonField(selfieJson, "facialComparisonDetails.riskLevel", "riskLevel");
    }

    private String resolveRejectionReason(String rootReason, String kycJson, String docJson, String selfieJson) {
        if (rootReason != null && !rootReason.isBlank()) return rootReason.trim();
        String reason = extractJsonField(kycJson, "rejectionReason", "rejectReason");
        if (reason != null) return reason;
        return extractJsonField(docJson, "rejectionReason");
    }

    private String resolveRemarks(String rootRemarks, String kycJson, String docJson, String selfieJson) {
        if (rootRemarks != null && !rootRemarks.isBlank()) return rootRemarks.trim();
        String rem = extractJsonField(kycJson, "remarks", "explanation", "summary");
        if (rem != null) return rem;
        rem = extractJsonField(selfieJson, "explanation", "message");
        if (rem != null) return rem;
        return extractJsonField(docJson, "message");
    }

    private String extractJsonField(String jsonStr, String... fieldPaths) {
        if (jsonStr == null || jsonStr.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(jsonStr);
            for (String path : fieldPaths) {
                if (path.contains(".")) {
                    String[] parts = path.split("\\.");
                    JsonNode current = root;
                    for (String part : parts) {
                        if (current != null) {
                            current = current.get(part);
                        }
                    }
                    if (current != null && !current.isNull() && !current.asText().isBlank()) {
                        return current.asText();
                    }
                } else {
                    JsonNode node = root.get(path);
                    if (node != null && !node.isNull() && !node.asText().isBlank()) {
                        return node.asText();
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Double extractJsonDouble(String jsonStr, String... fieldPaths) {
        if (jsonStr == null || jsonStr.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(jsonStr);
            for (String path : fieldPaths) {
                if (path.contains(".")) {
                    String[] parts = path.split("\\.");
                    JsonNode current = root;
                    for (String part : parts) {
                        if (current != null) {
                            current = current.get(part);
                        }
                    }
                    if (current != null && current.isNumber()) {
                        return current.asDouble();
                    }
                } else {
                    JsonNode node = root.get(path);
                    if (node != null && node.isNumber()) {
                        return node.asDouble();
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
