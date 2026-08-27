package com.bagusxmahendra.mltf.case_management_service.controller;

import com.bagusxmahendra.mltf.case_management_service.dto.CaseResponse;
import com.bagusxmahendra.mltf.case_management_service.dto.CreateCaseRequest;
import com.bagusxmahendra.mltf.case_management_service.dto.UpdateCaseStatusRequest;
import com.bagusxmahendra.mltf.case_management_service.model.CaseStatus;
import com.bagusxmahendra.mltf.case_management_service.service.CaseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/case")
public class CaseController {

    private static final Logger log = LoggerFactory.getLogger(CaseController.class);

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    /**
     * Create a new KYC case for IN_REVIEW applications.
     * <p>Receives KYC case details (for submissions flagged for manual compliance review), 2 GCS URLs
     * (document and selfie), document verification details, and selfie validation result,
     * then stores all data into Google Spanner case table in JSON format with status IN_PROGRESS.</p>
     * <p>Note: Automated APPROVED and REJECTED statuses do not create cases as they are processed automatically.</p>
     *
     * <pre>
     * POST /api/v1/case
     * </pre>
     */
    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<CaseResponse>> createCase(@Valid @RequestBody CreateCaseRequest request) {
        log.info("Received request to create case for userId: {}, caseType: {}, caseStatus: {}",
                request.getUserId(), request.getCaseType(), request.getCaseStatus());

        return caseService.createCase(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Get case by unique case ID.
     *
     * <pre>
     * GET /api/v1/case/{caseId}
     * </pre>
     */
    @GetMapping(value = "/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CaseResponse>> getCaseById(@PathVariable("caseId") String caseId) {
        log.info("Received request to fetch case with ID: {}", caseId);
        return caseService.getCaseById(caseId)
                .map(ResponseEntity::ok);
    }

    /**
     * Query cases by userId, status, or fetch all cases.
     *
     * <pre>
     * GET /api/v1/case?userId=usr_1001
     * GET /api/v1/case?status=IN_PROGRESS
     * GET /api/v1/case
     * </pre>
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CaseResponse> getCases(
            @RequestParam(name = "userId", required = false) String userIdParam,
            @RequestParam(name = "user_id", required = false) String userIdSnakeParam,
            @RequestParam(name = "status", required = false) String statusParam,
            @RequestParam(name = "caseStatus", required = false) String caseStatusParam,
            @RequestParam(name = "case_status", required = false) String caseStatusSnakeParam
    ) {
        String effectiveUserId = userIdParam != null ? userIdParam : userIdSnakeParam;
        String effectiveStatus = statusParam != null ? statusParam : (caseStatusParam != null ? caseStatusParam : caseStatusSnakeParam);

        if (effectiveUserId != null && !effectiveUserId.isBlank()) {
            log.info("Listing cases for userId: {}", effectiveUserId);
            return caseService.getCasesByUserId(effectiveUserId.trim());
        }

        if (effectiveStatus != null && !effectiveStatus.isBlank()) {
            CaseStatus status = CaseStatus.fromString(effectiveStatus);
            log.info("Listing cases for status: {}", status);
            return caseService.getCasesByStatus(status);
        }

        log.info("Listing all cases");
        return caseService.getAllCases();
    }

    /**
     * Update case status (IN_PROGRESS, ACCEPTED, REJECTED) and notes.
     *
     * <pre>
     * PATCH /api/v1/case/{caseId}/status
     * PUT /api/v1/case/{caseId}/status
     * </pre>
     */
    @RequestMapping(
            value = "/{caseId}/status",
            method = {RequestMethod.PATCH, RequestMethod.PUT},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<CaseResponse>> updateCaseStatus(
            @PathVariable("caseId") String caseId,
            @Valid @RequestBody UpdateCaseStatusRequest updateRequest
    ) {
        log.info("Updating status for caseId: {} to {}", caseId, updateRequest.getCaseStatus());
        return caseService.updateCaseStatus(caseId, updateRequest)
                .map(ResponseEntity::ok);
    }
}
