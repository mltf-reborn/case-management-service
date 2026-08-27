# Case Management Service

An enterprise-grade, reactive case management microservice built with **Java 25**, **Spring Boot (WebFlux)**, and **Google Cloud Spanner**, storing comprehensive KYC case records, multi-modal Google Cloud Storage (GCS) artifact URLs, document forensic verification results, and biometric selfie validation outcomes in native Spanner `JSON` format.

---

## 🌟 Key Capabilities & Business Rules

1. **Targeted KYC Case Creation for `IN_REVIEW` Status Only**:
   - Case creation via `/api/v1/case` is **exclusively for KYC submissions with `IN_REVIEW` status** (applications requiring manual compliance officer verification or falling below automated confidence thresholds).
   - Automated outcomes (**`APPROVED`** and **`REJECTED`**) are handled automatically by the AI supervisor pipeline and **do not generate a case** in the database.
   - When an `IN_REVIEW` case is submitted, it is persisted with `case_type = KYC` and initial status `case_status = IN_PROGRESS`.
   - Compliance officers can later review and update the case status to `ACCEPTED` or `REJECTED`.

2. **Native Google Cloud Spanner `JSON` Storage**:
   - Persists KYC case details, forensic document verification results, and biometric selfie validation data in native Spanner `JSON` columns (`kyc_details`, `document_verification_details`, `selfie_details`) for querying, reuse, and auditability.
   - Preserves complete raw payloads from upstream AI agents (`supervisor-agent` and `document-processing-agent`).

3. **Multi-Modal GCS Asset Tracking**:
   - Manages and links 2 Google Cloud Storage URLs per KYC case:
     - **File 1**: Identity Document URL (`document_url`, e.g., MyKad, Passport, Driver's License).
     - **File 2**: Biometric Selfie Photo URL (`selfie_url`, captured from user webcam).
   - Intelligently resolves and normalizes GCS URLs whether provided at the root request level or embedded within nested verification payloads.

4. **Integration with Multi-Agent Ecosystem**:
   - **Supervisor Agent (`supervisor-agent`)**: Routes `IN_REVIEW` submissions with consolidated KYC profiles, decision summaries, and external AML/sanctions registry checks to `/api/v1/case`.
   - **Document Processing Agent (`document-processing-agent`)**: Supplies forensic pixel tampering results, authenticity/originality scores, and extracted identity fields.
   - **Biometric Selfie Agent (`document-processing-agent`)**: Supplies facial comparison metrics, landmark matches, confidence scores, and liveness anti-spoofing results.

5. **High-Performance Non-Blocking Architecture**:
   - Built on Spring WebFlux and Project Reactor (`Mono`/`Flux`) with elastic thread pooling for asynchronous Spanner database operations.

---

## 🏗️ Architecture & Decision Routing

```
                           ┌───────────────────────────┐
                           │   KYC Verification Flow   │
                           │     (supervisor-agent)    │
                           └─────────────┬─────────────┘
                                         │
                        Evaluate Confidence & Compliance
                                         │
             ┌───────────────────────────┼───────────────────────────┐
             ▼                           ▼                           ▼
      ┌─────────────┐             ┌─────────────┐             ┌─────────────┐
      │  APPROVED   │             │  IN_REVIEW  │             │  REJECTED   │
      │ (Automatic) │             │ (Requires   │             │ (Automatic) │
      └──────┬──────┘             │  Compliance)│             └──────┬──────┘
             │                    └──────┬──────┘                    │
      Auto-Completed                     │                    Auto-Completed
     (No Case Created)                   ▼                   (No Case Created)
                              ┌─────────────────────┐
                              │ case-mgmt-service   │
                              │ (POST /api/v1/case) │
                              └──────────┬──────────┘
                                         │
                           Stores case with type = KYC
                            and status = IN_PROGRESS
                                         │
                                         ▼
                              ┌─────────────────────┐
                              │  Cloud Spanner DB   │
                              │     Table: case     │
                              └──────────┬──────────┘
                                         │
                               Compliance Officer
                             Manual Review Decision
                                         │
                              ┌──────────┴──────────┐
                              ▼                     ▼
                       ┌─────────────┐       ┌─────────────┐
                       │  ACCEPTED   │       │  REJECTED   │
                       │ (via PATCH) │       │ (via PATCH) │
                       └─────────────┘       └─────────────┘
```

---

## 🗄️ Database Schema & DDL (Google Cloud Spanner)

Schema script located at: [src/main/resources/db/spanner_schema.sql](file:///home/bagusmwicaksono/Projects/case-management-service/src/main/resources/db/spanner_schema.sql) and [db/spanner_schema.sql](file:///home/bagusmwicaksono/Projects/case-management-service/db/spanner_schema.sql).

### Table Schema Definition (Google Standard SQL)

```sql
-- Database: mortgage_db
-- Instance: mltf-spanner
-- Project:  mltf-506212

CREATE TABLE `case` (
    case_id STRING(64) NOT NULL,
    user_id STRING(64) NOT NULL,
    case_type STRING(32) NOT NULL,         -- Case Type: KYC
    case_status STRING(32) NOT NULL,       -- Case Status: IN_PROGRESS, ACCEPTED, REJECTED
    document_url STRING(1024),             -- Identity document GCS URL (GCS File 1)
    selfie_url STRING(1024),               -- Biometric selfie GCS URL (GCS File 2)
    document_verification_details JSON,    -- Document verification details in JSON format
    selfie_details JSON,                   -- Selfie validation details in JSON format
    kyc_details JSON,                      -- KYC case details in JSON format
    risk_score FLOAT64,                    -- Calculated risk score (0.0 - 100.0)
    risk_level STRING(32),                 -- Risk level: LOW, MEDIUM, HIGH, CRITICAL
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
```

### Applying DDL Schema via gcloud CLI

```bash
gcloud spanner databases ddl update mortgage_db \
    --instance=mltf-spanner \
    --project=mltf-506212 \
    --ddl="CREATE TABLE \`case\` (case_id STRING(64) NOT NULL, user_id STRING(64) NOT NULL, case_type STRING(32) NOT NULL, case_status STRING(32) NOT NULL, document_url STRING(1024), selfie_url STRING(1024), document_verification_details JSON, selfie_details JSON, kyc_details JSON, risk_score FLOAT64, risk_level STRING(32), rejection_reason STRING(1000), remarks STRING(1000), assigned_to STRING(64), created_at TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp = true), updated_at TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp = true)) PRIMARY KEY (case_id); CREATE INDEX idx_case_user_id ON \`case\`(user_id); CREATE INDEX idx_case_status ON \`case\`(case_status); CREATE INDEX idx_case_type ON \`case\`(case_type); CREATE INDEX idx_case_created_at ON \`case\`(created_at DESC);"
```

---

## ⚙️ Configuration & Environment Variables

| Environment Variable | Spring Property | Default | Description |
| :--- | :--- | :--- | :--- |
| `PORT` | `server.port` | `8082` | HTTP server port (avoids port collisions with 8080/8081) |
| `GCP_PROJECT_ID` | `gcp.spanner.project-id` | `mltf-506212` | Google Cloud Project ID |
| `GCP_SPANNER_INSTANCE_ID` | `gcp.spanner.instance-id` | `mltf-spanner` | Google Cloud Spanner Instance ID |
| `GCP_SPANNER_DATABASE_ID` | `gcp.spanner.database-id` | `mortgage_db` | Google Cloud Spanner Database Name |
| `SPANNER_EMULATOR_HOST` | `gcp.spanner.emulator-host` | *None* | Optional local Spanner emulator host (`host:port`) |

---

## 🚀 API Endpoints Reference

### 1. Create KYC Case (for `IN_REVIEW` submissions): `POST /api/v1/case`

Creates a new KYC case in Spanner with status `IN_PROGRESS` for applications flagged as `IN_REVIEW`.

> [!NOTE]
> Case creation is strictly reserved for `IN_REVIEW` KYC applications requiring manual inspection. Requests containing automated decisions (`APPROVED` or `REJECTED`) are rejected with `400 Bad Request` as those are handled automatically.

**Request Headers:**
- `Content-Type: application/json`

**Request Body (`application/json`):**
```json
{
  "userId": "usr_1001",
  "caseType": "KYC",
  "documentUrl": "gs://mltf-bucket/kyc/KYC-REV-2026-1001/document/mykad.jpg",
  "selfieUrl": "gs://mltf-bucket/kyc/KYC-REV-2026-1001/selfie/webcam.jpg",
  "kycDetails": {
    "fullName": "AHMAD SYAZWAN BIN ABDULLAH",
    "email": "ahmad.syazwan@example.com",
    "phoneNumber": "+60123456789",
    "idCardNumber": "940822-10-5819",
    "idCardType": "MyKad (National Identity Card)",
    "dateOfBirth": "1994-08-22",
    "address": "NO 12 JALAN MAJU 3, TAMAN BUKIT INDAH",
    "city": "JOHOR BAHRU",
    "postalCode": "79100",
    "country": "MALAYSIA",
    "nationality": "Malaysian",
    "occupation": "Software Engineer",
    "monthlyIncome": 8500.00,
    "status": "IN_REVIEW",
    "riskScore": 45.0,
    "riskLevel": "MEDIUM",
    "externalKycSummary": {
      "isIdentityVerified": true,
      "isBlacklisted": false,
      "amlSanctionsStatus": "PASS"
    }
  },
  "documentVerificationDetails": {
    "status": "SUCCESS",
    "message": "Document processed successfully",
    "detectedDocumentType": "MyKad (National Identity Card)",
    "scores": {
      "documentScore": 96.5,
      "originalityScore": 98.0,
      "confidenceScore": 95.0,
      "scoringBreakdown": "Originality: 98% (zero tampering); Confidence: 95%"
    },
    "pixelLevelCheck": {
      "isTampered": false,
      "tamperingRiskLevel": "LOW",
      "tamperingConfidence": 98.2,
      "findings": "Pixel analysis confirmed consistent font rendering and authentic background textures.",
      "anomalies": []
    },
    "extractedFields": {
      "fullName": "AHMAD SYAZWAN BIN ABDULLAH",
      "idNumber": "940822-10-5819",
      "dateOfBirth": "1994-08-22",
      "nationality": "Malaysian"
    }
  },
  "selfieDetails": {
    "status": "SUCCESS",
    "message": "Selfie validation completed successfully",
    "isIdentical": true,
    "confidenceScore": 88.5,
    "matchStatus": "MATCH",
    "explanation": "Biometric facial comparison falls slightly short of automated threshold (95.0%). Manual review required.",
    "facialComparisonDetails": {
      "faceDetectedInId": true,
      "faceDetectedInSelfie": true,
      "facialLandmarksMatch": true,
      "matchingFeatures": [
        "Identical jawline contour and chin shape",
        "Consistent interpupillary distance and ocular slant"
      ],
      "discrepantFeatures": [],
      "livenessCheck": {
        "isLive": true,
        "spoofRiskLevel": "NONE",
        "findings": "Authentic skin texture, zero screen replay or print attacks detected."
      },
      "riskLevel": "MEDIUM",
      "recommendation": "MANUAL_REVIEW"
    }
  },
  "remarks": "KYC verification requires manual compliance review. Biometric confidence score (88.5%) falls short of automated approval threshold (95.0%).",
  "assignedTo": "supervisor-agent-llm"
}
```

**Supported JSON Aliases:**
- `userId`: `user_id`, `applicantId`, `applicant_id`
- `documentUrl`: `document_url`, `idDocumentUrl`, `id_document_url`, `documentGcsUrl`, `idGcsUrl`, `docUrl`
- `selfieUrl`: `selfie_url`, `selfieGcsUrl`, `selfie_gcs_url`
- `documentVerificationDetails`: `document_verification_details`, `docVerificationDetails`, `doc_verification_details`, `documentProcessingResult`, `docProcessingResponse`
- `selfieDetails`: `selfie_details`, `selfieValidationDetails`, `selfie_validation_details`, `selfieResponse`
- `kycDetails`: `kyc_details`, `kycCaseDetails`, `kyc_case_details`, `kycProfile`, `profile`

**Response (`201 Created`):**
```json
{
  "caseId": "CASE-KYC-174000-5819",
  "userId": "usr_1001",
  "caseType": "KYC",
  "caseStatus": "IN_PROGRESS",
  "documentUrl": "gs://mltf-bucket/kyc/KYC-REV-2026-1001/document/mykad.jpg",
  "selfieUrl": "gs://mltf-bucket/kyc/KYC-REV-2026-1001/selfie/webcam.jpg",
  "documentVerificationDetails": { ... },
  "selfieDetails": { ... },
  "kycDetails": { ... },
  "riskScore": 45.0,
  "riskLevel": "MEDIUM",
  "remarks": "KYC verification requires manual compliance review. Biometric confidence score (88.5%) falls short of automated approval threshold (95.0%).",
  "assignedTo": "supervisor-agent-llm",
  "createdAt": "2026-08-27T11:35:00Z",
  "updatedAt": "2026-08-27T11:35:00Z"
}
```

---

### 2. Get Case by ID: `GET /api/v1/case/{caseId}`

**Example Request:**
```bash
curl -X GET http://localhost:8082/api/v1/case/CASE-KYC-174000-5819
```

**Response (`200 OK` or `404 Not Found`):** Returns the full `CaseResponse` object.

---

### 3. List & Filter Cases: `GET /api/v1/case`

**Query Parameters:**
- `userId` / `user_id`: Filter cases by user ID (`GET /api/v1/case?userId=usr_1001`)
- `status` / `caseStatus` / `case_status`: Filter cases by status (`GET /api/v1/case?status=IN_PROGRESS`)
- *(No params)*: Lists all cases ordered by creation time descending (`GET /api/v1/case`)

---

### 4. Update Case Status: `PATCH /api/v1/case/{caseId}/status` (or `PUT`)

Compliance officer manual decision update (`IN_PROGRESS` $\rightarrow$ `ACCEPTED` or `REJECTED`).

**Request Body (`application/json`):**
```json
{
  "caseStatus": "ACCEPTED",
  "remarks": "Manual compliance review completed; identity confirmed against national registry.",
  "assignedTo": "compliance_officer_01"
}
```

**Response (`200 OK`):** Returns the updated `CaseResponse` object.

---

## 🛠️ Build, Test & Run

### Run Unit Tests
```bash
./gradlew test
```

### Build Executable JAR
```bash
./gradlew bootJar
```

### Run Locally
```bash
./gradlew bootRun
```

### Deploy to Minikube
```bash
# Build JAR, package Docker image, load into Minikube, apply manifests, and verify rollout
./gradlew minikubeDeploy

# Undeploy from Minikube
./gradlew minikubeUndeploy
```
