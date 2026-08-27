package com.bagusxmahendra.mltf.case_management_service.controller;

import com.bagusxmahendra.mltf.case_management_service.dto.CaseResponse;
import com.bagusxmahendra.mltf.case_management_service.dto.CreateCaseRequest;
import com.bagusxmahendra.mltf.case_management_service.dto.UpdateCaseStatusRequest;
import com.bagusxmahendra.mltf.case_management_service.service.CaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseControllerTest {

    @Mock
    private CaseService caseService;

    @InjectMocks
    private CaseController caseController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(caseController)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createCase_validKycPayload_returns201Created() {
        Instant now = Instant.now();
        CaseResponse mockResponse = new CaseResponse(
                "CASE-KYC-1001",
                "usr_1001",
                "KYC",
                "IN_PROGRESS",
                "gs://mltf-bucket/kyc/document/id.jpg",
                "gs://mltf-bucket/kyc/selfie/photo.jpg",
                Map.of("status", "SUCCESS", "scores", Map.of("documentScore", 98.0)),
                Map.of("status", "SUCCESS", "isIdentical", true, "confidenceScore", 96.5),
                Map.of("fullName", "Ahmad Syazwan", "idCardNumber", "940822-10-5819"),
                5.0,
                "LOW",
                null,
                "Verification in progress",
                "supervisor-agent",
                now,
                now
        );

        when(caseService.createCase(any(CreateCaseRequest.class))).thenReturn(Mono.just(mockResponse));

        String requestJson = """
                {
                    "userId": "usr_1001",
                    "documentUrl": "gs://mltf-bucket/kyc/document/id.jpg",
                    "selfieUrl": "gs://mltf-bucket/kyc/selfie/photo.jpg",
                    "documentVerificationDetails": {
                        "status": "SUCCESS",
                        "detectedDocumentType": "MyKad",
                        "scores": {
                            "documentScore": 98.0,
                            "originalityScore": 99.0
                        }
                    },
                    "selfieDetails": {
                        "status": "SUCCESS",
                        "isIdentical": true,
                        "confidenceScore": 96.5,
                        "matchStatus": "MATCH"
                    },
                    "kycDetails": {
                        "fullName": "Ahmad Syazwan",
                        "idCardNumber": "940822-10-5819",
                        "nationality": "Malaysian",
                        "status": "IN_REVIEW"
                    }
                }
                """;

        webTestClient.post()
                .uri("/api/v1/case")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.caseId").isEqualTo("CASE-KYC-1001")
                .jsonPath("$.userId").isEqualTo("usr_1001")
                .jsonPath("$.caseType").isEqualTo("KYC")
                .jsonPath("$.caseStatus").isEqualTo("IN_PROGRESS")
                .jsonPath("$.documentUrl").isEqualTo("gs://mltf-bucket/kyc/document/id.jpg")
                .jsonPath("$.selfieUrl").isEqualTo("gs://mltf-bucket/kyc/selfie/photo.jpg")
                .jsonPath("$.documentVerificationDetails.status").isEqualTo("SUCCESS")
                .jsonPath("$.selfieDetails.isIdentical").isEqualTo(true)
                .jsonPath("$.kycDetails.fullName").isEqualTo("Ahmad Syazwan");
    }

    @Test
    void createCase_missingUserId_returns400BadRequest() {
        String invalidJson = """
                {
                    "documentUrl": "gs://mltf-bucket/doc.jpg",
                    "selfieUrl": "gs://mltf-bucket/selfie.jpg"
                }
                """;

        webTestClient.post()
                .uri("/api/v1/case")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCaseById_whenExists_returns200Ok() {
        Instant now = Instant.now();
        CaseResponse mockResponse = new CaseResponse(
                "CASE-KYC-1001",
                "usr_1001",
                "KYC",
                "IN_PROGRESS",
                "gs://doc",
                "gs://selfie",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "supervisor-agent",
                now,
                now
        );

        when(caseService.getCaseById("CASE-KYC-1001")).thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/api/v1/case/CASE-KYC-1001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.caseId").isEqualTo("CASE-KYC-1001")
                .jsonPath("$.caseType").isEqualTo("KYC")
                .jsonPath("$.caseStatus").isEqualTo("IN_PROGRESS");
    }

    @Test
    void getCaseById_whenNotFound_returns404NotFound() {
        when(caseService.getCaseById("CASE-NOT-FOUND"))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found")));

        webTestClient.get()
                .uri("/api/v1/case/CASE-NOT-FOUND")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getCases_byUserId_returnsCaseList() {
        Instant now = Instant.now();
        CaseResponse c1 = new CaseResponse("C1", "usr_1001", "KYC", "IN_PROGRESS", null, null, null, null, null, null, null, null, null, null, now, now);
        CaseResponse c2 = new CaseResponse("C2", "usr_1001", "KYC", "ACCEPTED", null, null, null, null, null, null, null, null, null, null, now, now);

        when(caseService.getCasesByUserId("usr_1001")).thenReturn(Flux.just(c1, c2));

        webTestClient.get()
                .uri("/api/v1/case?userId=usr_1001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].caseId").isEqualTo("C1")
                .jsonPath("$[1].caseId").isEqualTo("C2");
    }

    @Test
    void updateCaseStatus_validRequest_returnsUpdatedCase() {
        Instant now = Instant.now();
        CaseResponse updatedResponse = new CaseResponse(
                "CASE-1001",
                "usr_1001",
                "KYC",
                "ACCEPTED",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Approved",
                "admin",
                now,
                now
        );

        when(caseService.updateCaseStatus(eq("CASE-1001"), any(UpdateCaseStatusRequest.class)))
                .thenReturn(Mono.just(updatedResponse));

        String updateJson = """
                {
                    "caseStatus": "ACCEPTED",
                    "remarks": "Approved",
                    "assignedTo": "admin"
                }
                """;

        webTestClient.patch()
                .uri("/api/v1/case/CASE-1001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateJson)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.caseStatus").isEqualTo("ACCEPTED")
                .jsonPath("$.remarks").isEqualTo("Approved");
    }
}
