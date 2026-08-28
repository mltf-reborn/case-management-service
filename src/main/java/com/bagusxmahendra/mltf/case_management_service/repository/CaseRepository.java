package com.bagusxmahendra.mltf.case_management_service.repository;

import com.bagusxmahendra.mltf.case_management_service.model.CaseEntity;
import com.bagusxmahendra.mltf.case_management_service.model.CaseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CaseRepository {

    /**
     * Persist or update a case in the database.
     *
     * @param caseEntity the case entity to save
     * @return Mono<Void> indicating completion
     */
    Mono<Void> save(CaseEntity caseEntity);

    /**
     * Find case by unique case ID.
     *
     * @param caseId the unique case ID
     * @return Mono containing CaseEntity if found, empty Mono otherwise
     */
    Mono<CaseEntity> findById(String caseId);

    /**
     * Find all cases for a specific user ID.
     *
     * @param userId the user ID
     * @return Flux of CaseEntity matching the user ID
     */
    Flux<CaseEntity> findByUserId(String userId);

    /**
     * Find all cases by case status.
     *
     * @param status the case status (e.g. IN_PROGRESS, ACCEPTED, REJECTED)
     * @return Flux of CaseEntity matching the status
     */
    Flux<CaseEntity> findByStatus(CaseStatus status);

    /**
     * Find all cases ordered by creation timestamp descending.
     *
     * @return Flux of all CaseEntity records
     */
    Flux<CaseEntity> findAll();

    /**
     * Update the KYC profile status and associated audit fields in the kyc_profile table for a user.
     *
     * @param userId the user ID associated with the KYC profile
     * @param status the updated status (e.g. IN_PROGRESS, ACCEPTED, REJECTED)
     * @param remarks case summary remarks or compliance notes
     * @param rejectionReason rejection reason if rejected
     * @param verifiedBy identifier of the reviewer or officer
     * @return Mono<Void> indicating completion
     */
    Mono<Void> updateKycProfileStatus(String userId, String status, String remarks, String rejectionReason, String verifiedBy);

    /**
     * Update the KYC profile status for a user.
     *
     * @param userId the user ID associated with the KYC profile
     * @param status the updated status (e.g. IN_PROGRESS, ACCEPTED, REJECTED)
     * @return Mono<Void> indicating completion
     */
    default Mono<Void> updateKycProfileStatus(String userId, String status) {
        return updateKycProfileStatus(userId, status, null, null, null);
    }
}
