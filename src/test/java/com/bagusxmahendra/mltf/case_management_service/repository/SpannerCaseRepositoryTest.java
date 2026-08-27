package com.bagusxmahendra.mltf.case_management_service.repository;

import com.bagusxmahendra.mltf.case_management_service.model.CaseEntity;
import com.bagusxmahendra.mltf.case_management_service.model.CaseStatus;
import com.bagusxmahendra.mltf.case_management_service.model.CaseType;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpannerCaseRepositoryTest {

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private ReadContext readContext;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Struct struct;

    private SpannerCaseRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SpannerCaseRepository(databaseClient);
    }

    @Test
    void save_validEntity_writesMutationToDatabaseClient() {
        Instant now = Instant.now();
        CaseEntity entity = new CaseEntity(
                "CASE-KYC-1",
                "usr_1001",
                CaseType.KYC,
                CaseStatus.IN_PROGRESS,
                "gs://bucket/doc.jpg",
                "gs://bucket/selfie.jpg",
                "{\"status\":\"SUCCESS\"}",
                "{\"isIdentical\":true}",
                "{\"fullName\":\"John Doe\"}",
                15.0,
                "LOW",
                null,
                "Case initiated",
                "supervisor-agent",
                now,
                now
        );

        when(databaseClient.write(any())).thenReturn(Timestamp.now());

        StepVerifier.create(repository.save(entity))
                .verifyComplete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Mutation>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(databaseClient).write(captor.capture());

        Iterable<Mutation> mutations = captor.getValue();
        assertThat(mutations).isNotEmpty();
    }

    @Test
    void findById_whenRecordExists_returnsCaseEntity() {
        when(databaseClient.singleUse()).thenReturn(readContext);
        when(readContext.executeQuery(any(Statement.class))).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getCurrentRowAsStruct()).thenReturn(struct);

        when(struct.isNull("case_id")).thenReturn(false);
        when(struct.getString("case_id")).thenReturn("CASE-KYC-1");

        when(struct.isNull("user_id")).thenReturn(false);
        when(struct.getString("user_id")).thenReturn("usr_1001");

        when(struct.isNull("case_type")).thenReturn(false);
        when(struct.getString("case_type")).thenReturn("KYC");

        when(struct.isNull("case_status")).thenReturn(false);
        when(struct.getString("case_status")).thenReturn("IN_PROGRESS");

        when(struct.isNull("document_url")).thenReturn(false);
        when(struct.getString("document_url")).thenReturn("gs://doc");

        when(struct.isNull("selfie_url")).thenReturn(false);
        when(struct.getString("selfie_url")).thenReturn("gs://selfie");

        when(struct.isNull("document_verification_details")).thenReturn(false);
        when(struct.getJson("document_verification_details")).thenReturn("{\"status\":\"SUCCESS\"}");

        when(struct.isNull("selfie_details")).thenReturn(false);
        when(struct.getJson("selfie_details")).thenReturn("{\"isIdentical\":true}");

        when(struct.isNull("kyc_details")).thenReturn(false);
        when(struct.getJson("kyc_details")).thenReturn("{\"fullName\":\"John Doe\"}");

        when(struct.isNull("risk_score")).thenReturn(false);
        when(struct.getDouble("risk_score")).thenReturn(12.5);

        when(struct.isNull("risk_level")).thenReturn(false);
        when(struct.getString("risk_level")).thenReturn("LOW");

        when(struct.isNull("rejection_reason")).thenReturn(true);
        when(struct.isNull("remarks")).thenReturn(false);
        when(struct.getString("remarks")).thenReturn("All good");

        when(struct.isNull("assigned_to")).thenReturn(false);
        when(struct.getString("assigned_to")).thenReturn("supervisor");

        when(struct.isNull("created_at")).thenReturn(false);
        when(struct.getTimestamp("created_at")).thenReturn(Timestamp.now());

        when(struct.isNull("updated_at")).thenReturn(false);
        when(struct.getTimestamp("updated_at")).thenReturn(Timestamp.now());

        StepVerifier.create(repository.findById("CASE-KYC-1"))
                .assertNext(found -> {
                    assertThat(found.caseId()).isEqualTo("CASE-KYC-1");
                    assertThat(found.userId()).isEqualTo("usr_1001");
                    assertThat(found.caseType()).isEqualTo(CaseType.KYC);
                    assertThat(found.caseStatus()).isEqualTo(CaseStatus.IN_PROGRESS);
                    assertThat(found.riskScore()).isEqualTo(12.5);
                })
                .verifyComplete();
    }

    @Test
    void findById_whenRecordDoesNotExist_returnsEmpty() {
        when(databaseClient.singleUse()).thenReturn(readContext);
        when(readContext.executeQuery(any(Statement.class))).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        StepVerifier.create(repository.findById("CASE-NOT-EXIST"))
                .verifyComplete();
    }
}
