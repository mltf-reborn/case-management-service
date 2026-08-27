-- ==============================================================================
-- Google Cloud Spanner Schema Definition: case table
-- Database: mortgage_db
-- Instance: mltf-spanner
-- Project: mltf-506212
-- ==============================================================================
--
-- Business Rule:
--   Case creation is exclusively triggered for KYC submissions with IN_REVIEW status
--   (requiring manual compliance verification). Automated APPROVED and REJECTED
--   statuses are processed automatically and do not generate a case in the database.
--   Cases are initially stored with case_status = IN_PROGRESS and transitioned to
--   ACCEPTED or REJECTED upon compliance review.
--
-- How to apply this schema via gcloud CLI:
--
--   gcloud spanner databases ddl update mortgage_db \
--       --instance=mltf-spanner \
--       --project=mltf-506212 \
--       --ddl="CREATE TABLE \`case\` (case_id STRING(64) NOT NULL, user_id STRING(64) NOT NULL, case_type STRING(32) NOT NULL, case_status STRING(32) NOT NULL, document_url STRING(1024), selfie_url STRING(1024), document_verification_details JSON, selfie_details JSON, kyc_details JSON, risk_score FLOAT64, risk_level STRING(32), rejection_reason STRING(1000), remarks STRING(1000), assigned_to STRING(64), created_at TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp = true), updated_at TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp = true)) PRIMARY KEY (case_id); CREATE INDEX idx_case_user_id ON \`case\`(user_id); CREATE INDEX idx_case_status ON \`case\`(case_status); CREATE INDEX idx_case_type ON \`case\`(case_type); CREATE INDEX idx_case_created_at ON \`case\`(created_at DESC);"
--
-- ------------------------------------------------------------------------------
-- 1. Google Standard SQL Dialect (Spanner Default)
-- ------------------------------------------------------------------------------

CREATE TABLE `case` (
    case_id STRING(64) NOT NULL,
    user_id STRING(64) NOT NULL,
    case_type STRING(32) NOT NULL,         -- Case Type: KYC
    case_status STRING(32) NOT NULL,       -- Case Status: IN_PROGRESS, ACCEPTED, REJECTED
    document_url STRING(1024),             -- Identity document GCS URL (GCS File 1)
    selfie_url STRING(1024),               -- Biometric selfie GCS URL (GCS File 2)
    document_verification_details JSON,    -- Document verification details (from document-processing-agent) in JSON format
    selfie_details JSON,                   -- Selfie validation details (from document-processing-agent) in JSON format
    kyc_details JSON,                      -- KYC case details (from supervisor-agent) in JSON format
    risk_score FLOAT64,                    -- Calculated risk score (e.g. 0.0 - 100.0)
    risk_level STRING(32),                 -- Risk level (LOW, MEDIUM, HIGH, CRITICAL)
    rejection_reason STRING(1000),         -- Rejection reason explanation
    remarks STRING(1000),                  -- Case summary remarks & audit notes
    assigned_to STRING(64),                -- Reviewer / Processor / Agent identifier
    created_at TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp = true),
    updated_at TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp = true)
) PRIMARY KEY (case_id);

-- Secondary Indexes for fast lookups & filtering
CREATE INDEX idx_case_user_id ON `case`(user_id);
CREATE INDEX idx_case_status ON `case`(case_status);
CREATE INDEX idx_case_type ON `case`(case_type);
CREATE INDEX idx_case_created_at ON `case`(created_at DESC);

-- ------------------------------------------------------------------------------
-- 2. Sample Data Insertion (Google Standard SQL) - IN_REVIEW Submission
-- ------------------------------------------------------------------------------
/*
INSERT INTO `case` (
    case_id, user_id, case_type, case_status,
    document_url, selfie_url,
    document_verification_details, selfie_details, kyc_details,
    risk_score, risk_level, rejection_reason, remarks, assigned_to,
    created_at, updated_at
) VALUES (
    'CASE-KYC-1001',
    'usr_1001',
    'KYC',
    'IN_PROGRESS',
    'gs://mltf-bucket/kyc/KYC-REV-2026-1001/document/mykad.jpg',
    'gs://mltf-bucket/kyc/KYC-REV-2026-1001/selfie/webcam.jpg',
    JSON '{"status":"SUCCESS","scores":{"documentScore":96.5,"originalityScore":98.0,"confidenceScore":95.0},"pixelLevelCheck":{"isTampered":false,"tamperingRiskLevel":"LOW"},"detectedDocumentType":"MyKad (National Identity Card)"}',
    JSON '{"status":"SUCCESS","isIdentical":true,"confidenceScore":88.5,"matchStatus":"MATCH","explanation":"Biometric score falls short of automated threshold (95.0%). Manual review required."}',
    JSON '{"fullName":"AHMAD SYAZWAN BIN ABDULLAH","idCardNumber":"940822-10-5819","idCardType":"MyKad","dateOfBirth":"1994-08-22","nationality":"Malaysian","status":"IN_REVIEW"}',
    45.0,
    'MEDIUM',
    NULL,
    'KYC verification requires manual compliance review. Biometric confidence score (88.5%) falls short of automated approval threshold (95.0%).',
    'supervisor-agent-llm',
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP()
);
*/

-- ------------------------------------------------------------------------------
-- 3. Alternative PostgreSQL Dialect (if Spanner database uses PostgreSQL dialect)
-- ------------------------------------------------------------------------------
/*
CREATE TABLE "case" (
    case_id VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    case_type VARCHAR(32) NOT NULL,
    case_status VARCHAR(32) NOT NULL,
    document_url VARCHAR(1024),
    selfie_url VARCHAR(1024),
    document_verification_details JSONB,
    selfie_details JSONB,
    kyc_details JSONB,
    risk_score DOUBLE PRECISION,
    risk_level VARCHAR(32),
    rejection_reason VARCHAR(1000),
    remarks VARCHAR(1000),
    assigned_to VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_case_user_id ON "case"(user_id);
CREATE INDEX idx_case_status ON "case"(case_status);
CREATE INDEX idx_case_type ON "case"(case_type);
CREATE INDEX idx_case_created_at ON "case"(created_at DESC);
*/
