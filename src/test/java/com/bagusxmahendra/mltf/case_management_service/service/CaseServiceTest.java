package com.bagusxmahendra.mltf.case_management_service.service;

import com.bagusxmahendra.mltf.case_management_service.dto.CaseResponse;
import com.bagusxmahendra.mltf.case_management_service.dto.CreateCaseRequest;
import com.bagusxmahendra.mltf.case_management_service.dto.UpdateCaseStatusRequest;
import com.bagusxmahendra.mltf.case_management_service.model.CaseEntity;
import com.bagusxmahendra.mltf.case_management_service.model.CaseStatus;
import com.bagusxmahendra.mltf.case_management_service.model.CaseType;
import com.bagusxmahendra.mltf.case_management_service.repository.CaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    private ObjectMapper objectMapper;
    private CaseService caseService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        caseService = new CaseService(caseRepository, objectMapper);
    }

    @Test
    void createCase_withInReviewKycDetails_persistsAsInProgressKycCase() {
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(Mono.empty());

        CreateCaseRequest request = new CreateCaseRequest();
        request.setUserId("usr_1001");
        request.setDocumentUrl("gs://mltf-bucket/kyc/session-1/document/id.jpg");
        request.setSelfieUrl("gs://mltf-bucket/kyc/session-1/selfie/photo.jpg");

        Map<String, Object> docVerification = Map.of(
                "status", "SUCCESS",
                "detectedDocumentType", "MyKad",
                "scores", Map.of("documentScore", 98.5, "originalityScore", 99.0)
        );
        request.setDocumentVerificationDetails(docVerification);

        Map<String, Object> selfieDetails = Map.of(
                "status", "SUCCESS",
                "isIdentical", true,
                "confidenceScore", 97.2,
                "matchStatus", "MATCH"
        );
        request.setSelfieDetails(selfieDetails);

        Map<String, Object> kycDetails = Map.of(
                "fullName", "Ahmad Syazwan",
                "idCardNumber", "940822-10-5819",
                "nationality", "Malaysian",
                "status", "IN_REVIEW"
        );
        request.setKycDetails(kycDetails);

        StepVerifier.create(caseService.createCase(request))
                .assertNext(response -> {
                    assertThat(response.caseId()).startsWith("CASE-KYC-");
                    assertThat(response.userId()).isEqualTo("usr_1001");
                    assertThat(response.caseType()).isEqualTo("KYC");
                    assertThat(response.caseStatus()).isEqualTo("IN_PROGRESS");
                    assertThat(response.documentUrl()).isEqualTo("gs://mltf-bucket/kyc/session-1/document/id.jpg");
                    assertThat(response.selfieUrl()).isEqualTo("gs://mltf-bucket/kyc/session-1/selfie/photo.jpg");
                    assertThat(response.documentVerificationDetails()).isNotNull();
                    assertThat(response.selfieDetails()).isNotNull();
                    assertThat(response.kycDetails()).isNotNull();
                })
                .verifyComplete();

        ArgumentCaptor<CaseEntity> captor = ArgumentCaptor.forClass(CaseEntity.class);
        verify(caseRepository).save(captor.capture());
        CaseEntity saved = captor.getValue();
        assertThat(saved.caseType()).isEqualTo(CaseType.KYC);
        assertThat(saved.caseStatus()).isEqualTo(CaseStatus.IN_PROGRESS);
        assertThat(saved.documentVerificationDetails()).contains("\"status\":\"SUCCESS\"");
        assertThat(saved.selfieDetails()).contains("\"confidenceScore\":97.2");
        assertThat(saved.kycDetails()).contains("\"fullName\":\"Ahmad Syazwan\"");
    }

    @Test
    void createCase_withLoanApplicationCaseType_persistsAsLoanApplicationCase() {
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(Mono.empty());

        CreateCaseRequest request = new CreateCaseRequest();
        request.setUserId("usr_1002");
        request.setCaseType("LOAN_APPLICATION");

        StepVerifier.create(caseService.createCase(request))
                .assertNext(response -> {
                    assertThat(response.userId()).isEqualTo("usr_1002");
                    assertThat(response.caseType()).isEqualTo("LOAN_APPLICATION");
                    assertThat(response.caseStatus()).isEqualTo("IN_PROGRESS");
                })
                .verifyComplete();

        ArgumentCaptor<CaseEntity> captor = ArgumentCaptor.forClass(CaseEntity.class);
        verify(caseRepository).save(captor.capture());
        CaseEntity saved = captor.getValue();
        assertThat(saved.caseType()).isEqualTo(CaseType.LOAN_APPLICATION);
        assertThat(saved.caseStatus()).isEqualTo(CaseStatus.IN_PROGRESS);
    }

    @Test
    void createCase_withApprovedKycDetails_returnsBadRequest() {
        CreateCaseRequest request = new CreateCaseRequest();
        request.setUserId("usr_1001");
        request.setKycDetails(Map.of(
                "fullName", "John Doe",
                "status", "APPROVED"
        ));

        StepVerifier.create(caseService.createCase(request))
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException rse
                        && rse.getMessage().contains("APPROVED applications are processed automatically"))
                .verify();

        verify(caseRepository, never()).save(any());
    }

    @Test
    void createCase_withRejectedKycDetails_returnsBadRequest() {
        CreateCaseRequest request = new CreateCaseRequest();
        request.setUserId("usr_1001");
        request.setKycDetails(Map.of(
                "fullName", "Fraud Applicant",
                "status", "REJECTED"
        ));

        StepVerifier.create(caseService.createCase(request))
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException rse
                        && rse.getMessage().contains("REJECTED applications are processed automatically"))
                .verify();

        verify(caseRepository, never()).save(any());
    }

    @Test
    void createCase_withExplicitAcceptedCaseStatus_returnsBadRequest() {
        CreateCaseRequest request = new CreateCaseRequest();
        request.setUserId("usr_1001");
        request.setCaseStatus("ACCEPTED");

        StepVerifier.create(caseService.createCase(request))
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException rse
                        && rse.getMessage().contains("New cases can only be created with IN_PROGRESS status"))
                .verify();

        verify(caseRepository, never()).save(any());
    }

    @Test
    void createCase_withNestedGcsUrls_extractsUrlsCorrectly() {
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(Mono.empty());

        CreateCaseRequest request = new CreateCaseRequest();
        request.setUserId("usr_1002");
        request.setDocumentVerificationDetails(Map.of("gcsUrl", "gs://mltf-bucket/doc.jpg"));
        request.setSelfieDetails(Map.of("selfieUrl", "gs://mltf-bucket/selfie.jpg"));

        StepVerifier.create(caseService.createCase(request))
                .assertNext(response -> {
                    assertThat(response.documentUrl()).isEqualTo("gs://mltf-bucket/doc.jpg");
                    assertThat(response.selfieUrl()).isEqualTo("gs://mltf-bucket/selfie.jpg");
                })
                .verifyComplete();
    }

    @Test
    void createCase_missingUserId_returnsBadRequest() {
        CreateCaseRequest request = new CreateCaseRequest();

        StepVerifier.create(caseService.createCase(request))
                .expectError(ResponseStatusException.class)
                .verify();

        verify(caseRepository, never()).save(any());
    }

    @Test
    void getCaseById_whenFound_returnsCase() {
        Instant now = Instant.now();
        CaseEntity entity = new CaseEntity(
                "CASE-123",
                "usr_1001",
                CaseType.KYC,
                CaseStatus.IN_PROGRESS,
                "gs://doc",
                "gs://selfie",
                "{\"status\":\"SUCCESS\"}",
                "{\"isIdentical\":true}",
                "{\"fullName\":\"John\"}",
                null,
                10.0,
                "LOW",
                null,
                "Remarks",
                "supervisor",
                now,
                now
        );
        when(caseRepository.findById("CASE-123")).thenReturn(Mono.just(entity));

        StepVerifier.create(caseService.getCaseById("CASE-123"))
                .assertNext(resp -> {
                    assertThat(resp.caseId()).isEqualTo("CASE-123");
                    assertThat(resp.userId()).isEqualTo("usr_1001");
                    assertThat(resp.caseStatus()).isEqualTo("IN_PROGRESS");
                })
                .verifyComplete();
    }

    @Test
    void getCaseById_whenNotFound_returnsNotFound() {
        when(caseRepository.findById("CASE-999")).thenReturn(Mono.empty());

        StepVerifier.create(caseService.getCaseById("CASE-999"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void updateCaseStatus_toAccepted_updatesAndSavesCaseAndProfile() {
        Instant now = Instant.now();
        CaseEntity existing = new CaseEntity(
                "CASE-123",
                "usr_1001",
                CaseType.KYC,
                CaseStatus.IN_PROGRESS,
                "gs://doc",
                "gs://selfie",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "supervisor",
                now,
                now
        );
        when(caseRepository.findById("CASE-123")).thenReturn(Mono.just(existing));
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(Mono.empty());
        when(caseRepository.updateKycProfileStatus(anyString(), anyString(), any(), any(), any())).thenReturn(Mono.empty());

        UpdateCaseStatusRequest updateRequest = new UpdateCaseStatusRequest("ACCEPTED", "Approved after review", null, "admin_user");

        StepVerifier.create(caseService.updateCaseStatus("CASE-123", updateRequest))
                .assertNext(resp -> {
                    assertThat(resp.caseStatus()).isEqualTo("ACCEPTED");
                    assertThat(resp.remarks()).isEqualTo("Approved after review");
                    assertThat(resp.assignedTo()).isEqualTo("admin_user");
                })
                .verifyComplete();

        verify(caseRepository).save(any(CaseEntity.class));
        verify(caseRepository).updateKycProfileStatus("usr_1001", "ACCEPTED", "Approved after review", null, "admin_user");
    }

    @Test
    void updateCaseStatus_toRejected_updatesAndSavesCaseAndProfile() {
        Instant now = Instant.now();
        CaseEntity existing = new CaseEntity(
                "CASE-456",
                "usr_2002",
                CaseType.KYC,
                CaseStatus.IN_PROGRESS,
                "gs://doc",
                "gs://selfie",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "supervisor",
                now,
                now
        );
        when(caseRepository.findById("CASE-456")).thenReturn(Mono.just(existing));
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(Mono.empty());
        when(caseRepository.updateKycProfileStatus(anyString(), anyString(), any(), any(), any())).thenReturn(Mono.empty());

        UpdateCaseStatusRequest updateRequest = new UpdateCaseStatusRequest("REJECTED", "Tampered ID detected", "Document forgery suspected", "compliance_officer");

        StepVerifier.create(caseService.updateCaseStatus("CASE-456", updateRequest))
                .assertNext(resp -> {
                    assertThat(resp.caseStatus()).isEqualTo("REJECTED");
                    assertThat(resp.remarks()).isEqualTo("Tampered ID detected");
                    assertThat(resp.rejectionReason()).isEqualTo("Document forgery suspected");
                    assertThat(resp.assignedTo()).isEqualTo("compliance_officer");
                })
                .verifyComplete();

        verify(caseRepository).save(any(CaseEntity.class));
        verify(caseRepository).updateKycProfileStatus("usr_2002", "REJECTED", "Tampered ID detected", "Document forgery suspected", "compliance_officer");
    }
}
