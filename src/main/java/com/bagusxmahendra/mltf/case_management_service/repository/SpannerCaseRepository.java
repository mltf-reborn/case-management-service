package com.bagusxmahendra.mltf.case_management_service.repository;

import com.bagusxmahendra.mltf.case_management_service.model.CaseEntity;
import com.bagusxmahendra.mltf.case_management_service.model.CaseStatus;
import com.bagusxmahendra.mltf.case_management_service.model.CaseType;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class SpannerCaseRepository implements CaseRepository {

    private static final Logger log = LoggerFactory.getLogger(SpannerCaseRepository.class);

    private static final String SELECT_CASE_COLUMNS =
            "SELECT case_id, user_id, case_type, case_status, document_url, selfie_url, " +
            "document_verification_details, selfie_details, kyc_details, " +
            "risk_score, risk_level, rejection_reason, remarks, assigned_to, " +
            "created_at, updated_at " +
            "FROM `case` ";

    private final DatabaseClient databaseClient;

    public SpannerCaseRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Void> save(CaseEntity caseEntity) {
        return Mono.fromRunnable(() -> {
            log.debug("Saving case to Spanner for caseId: {}, userId: {}, status: {}",
                    caseEntity.caseId(), caseEntity.userId(), caseEntity.caseStatus());

            Mutation.WriteBuilder builder = Mutation.newInsertOrUpdateBuilder("case")
                    .set("case_id").to(caseEntity.caseId())
                    .set("user_id").to(caseEntity.userId())
                    .set("case_type").to(caseEntity.caseType() != null ? caseEntity.caseType().name() : CaseType.KYC.name())
                    .set("case_status").to(caseEntity.caseStatus() != null ? caseEntity.caseStatus().name() : CaseStatus.IN_PROGRESS.name())
                    .set("document_url").to(caseEntity.documentUrl())
                    .set("selfie_url").to(caseEntity.selfieUrl())
                    .set("document_verification_details").to(Value.json(caseEntity.documentVerificationDetails()))
                    .set("selfie_details").to(Value.json(caseEntity.selfieDetails()))
                    .set("kyc_details").to(Value.json(caseEntity.kycDetails()))
                    .set("risk_score").to(caseEntity.riskScore())
                    .set("risk_level").to(caseEntity.riskLevel())
                    .set("rejection_reason").to(caseEntity.rejectionReason())
                    .set("remarks").to(caseEntity.remarks())
                    .set("assigned_to").to(caseEntity.assignedTo())
                    .set("updated_at").to(Value.COMMIT_TIMESTAMP);

            if (caseEntity.createdAt() != null) {
                builder.set("created_at").to(Timestamp.ofTimeSecondsAndNanos(
                        caseEntity.createdAt().getEpochSecond(),
                        caseEntity.createdAt().getNano()
                ));
            } else {
                builder.set("created_at").to(Value.COMMIT_TIMESTAMP);
            }

            databaseClient.write(Collections.singletonList(builder.build()));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    @Override
    public Mono<CaseEntity> findById(String caseId) {
        return Mono.fromCallable(() -> {
            log.debug("Executing Spanner query to find case by caseId: {}", caseId);
            Statement statement = Statement.newBuilder(SELECT_CASE_COLUMNS + "WHERE case_id = @caseId")
                    .bind("caseId").to(caseId)
                    .build();

            try (ResultSet rs = databaseClient.singleUse().executeQuery(statement)) {
                if (rs.next()) {
                    return CaseEntity.fromStruct(rs.getCurrentRowAsStruct());
                }
                return null;
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty);
    }

    @Override
    public Flux<CaseEntity> findByUserId(String userId) {
        return Mono.fromCallable(() -> {
            log.debug("Executing Spanner query to find cases by userId: {}", userId);
            Statement statement = Statement.newBuilder(SELECT_CASE_COLUMNS + "WHERE user_id = @userId ORDER BY created_at DESC")
                    .bind("userId").to(userId)
                    .build();

            List<CaseEntity> cases = new ArrayList<>();
            try (ResultSet rs = databaseClient.singleUse().executeQuery(statement)) {
                while (rs.next()) {
                    cases.add(CaseEntity.fromStruct(rs.getCurrentRowAsStruct()));
                }
            }
            return cases;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<CaseEntity> findByStatus(CaseStatus status) {
        return Mono.fromCallable(() -> {
            log.debug("Executing Spanner query to find cases by status: {}", status);
            Statement statement = Statement.newBuilder(SELECT_CASE_COLUMNS + "WHERE case_status = @status ORDER BY created_at DESC")
                    .bind("status").to(status.name())
                    .build();

            List<CaseEntity> cases = new ArrayList<>();
            try (ResultSet rs = databaseClient.singleUse().executeQuery(statement)) {
                while (rs.next()) {
                    cases.add(CaseEntity.fromStruct(rs.getCurrentRowAsStruct()));
                }
            }
            return cases;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<CaseEntity> findAll() {
        return Mono.fromCallable(() -> {
            log.debug("Executing Spanner query to find all cases");
            Statement statement = Statement.newBuilder(SELECT_CASE_COLUMNS + "ORDER BY created_at DESC")
                    .build();

            List<CaseEntity> cases = new ArrayList<>();
            try (ResultSet rs = databaseClient.singleUse().executeQuery(statement)) {
                while (rs.next()) {
                    cases.add(CaseEntity.fromStruct(rs.getCurrentRowAsStruct()));
                }
            }
            return cases;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }
}
