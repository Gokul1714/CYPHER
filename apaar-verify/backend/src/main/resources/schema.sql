-- =============================================================================
-- CYPHER: AICTE Regulatory Officer & Student Data Verification Platform
-- Database Schema for PostgreSQL
-- All India Council for Technical Education (AICTE), Government of India
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. INSTITUTIONS
CREATE TABLE institutions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aicte_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    state VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    institution_type VARCHAR(100) NOT NULL, -- e.g. Central Univ, State Govt, Private-Self Financed
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. USERS & ROLES
CREATE TYPE user_role AS ENUM ('ROLE_AICTE_ADMIN', 'ROLE_INSTITUTION_ADMIN', 'ROLE_VERIFIER');

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    staff_id VARCHAR(100) UNIQUE,
    department VARCHAR(100),
    is_approved BOOLEAN DEFAULT TRUE,
    role user_role NOT NULL,
    institution_id UUID REFERENCES institutions(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. APPROVED INTAKE QUOTAS
CREATE TABLE approved_intake (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    course_name VARCHAR(150) NOT NULL,         -- e.g. B.Tech Computer Science & Engineering
    department VARCHAR(100) NOT NULL,          -- e.g. Engineering & Technology
    academic_year VARCHAR(20) NOT NULL,        -- e.g. 2026-2027
    approved_seats INT NOT NULL CHECK (approved_seats >= 0),
    nri_quota INT DEFAULT 0 CHECK (nri_quota >= 0),
    supernumerary_quota INT DEFAULT 0 CHECK (supernumerary_quota >= 0),
    approval_ref_number VARCHAR(100),          -- AICTE approval letter reference
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_institution_course_year UNIQUE (institution_id, course_name, academic_year)
);

-- 4. STUDENTS (INTERNAL MASTER RECORD)
-- Note: Internal UUID primary key used. Aadhaar is strictly NOT collected.
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_name VARCHAR(200) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    apaar_id VARCHAR(50),                      -- 12-digit APAAR ID (optional on preliminary upload)
    apaar_format_valid BOOLEAN DEFAULT FALSE,  -- Result of format & check-digit validation
    apaar_verification_status VARCHAR(50) DEFAULT 'UNVERIFIED', -- UNVERIFIED, MOCK_VERIFIED, PENDING_OFFICIAL_GATEWAY, REJECTED
    father_or_guardian_name VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_students_apaar_id ON students(apaar_id);

-- 5. ENROLMENTS (MAPPING STUDENTS TO INSTITUTION, PROGRAM, AND ACADEMIC YEAR)
-- Supports transfers, course change, and multi-year tracking
CREATE TYPE enrolment_status_enum AS ENUM ('ACTIVE', 'COMPLETED', 'TRANSFERRED', 'WITHDRAWN', 'SUSPENDED');
CREATE TYPE admission_category_enum AS ENUM ('GENERAL', 'OBC_NCL', 'SC', 'ST', 'EWS', 'MANAGEMENT', 'NRI', 'FOREIGN_NATIONAL');

CREATE TABLE enrolments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    course_name VARCHAR(150) NOT NULL,
    department VARCHAR(100) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    roll_number VARCHAR(100),
    enrolment_status enrolment_status_enum NOT NULL DEFAULT 'ACTIVE',
    admission_category admission_category_enum NOT NULL DEFAULT 'GENERAL',
    is_nri BOOLEAN DEFAULT FALSE,
    is_oci BOOLEAN DEFAULT FALSE,
    is_foreign_national BOOLEAN DEFAULT FALSE,
    is_supernumerary BOOLEAN DEFAULT FALSE,
    admission_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_institution_year UNIQUE (student_id, institution_id, academic_year)
);

CREATE INDEX idx_enrolments_institution ON enrolments(institution_id, academic_year);
CREATE INDEX idx_enrolments_student ON enrolments(student_id);

-- 6. VALIDATION RESULTS & ANOMALY ALERTS
CREATE TYPE validation_severity AS ENUM ('INFO', 'WARNING', 'ERROR', 'CRITICAL');
CREATE TYPE validation_category AS ENUM (
    'MISSING_APAAR',
    'INVALID_APAAR_FORMAT',
    'INTRA_INSTITUTION_DUPLICATE',
    'INTER_INSTITUTION_DUPLICATE',
    'EXCESS_ADMISSION_INTAKE',
    'MISSING_MANDATORY_FIELD',
    'CATEGORY_MISMATCH'
);

CREATE TABLE validation_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID REFERENCES institutions(id) ON DELETE CASCADE,
    student_id UUID REFERENCES students(id) ON DELETE CASCADE,
    enrolment_id UUID REFERENCES enrolments(id) ON DELETE CASCADE,
    rule_category validation_category NOT NULL,
    severity validation_severity NOT NULL,
    message TEXT NOT NULL,
    details JSONB,                              -- Additional context (e.g. conflicting institution codes)
    resolved BOOLEAN DEFAULT FALSE,
    resolved_by UUID REFERENCES users(id),
    resolution_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_validation_inst ON validation_results(institution_id, resolved);

-- 7. SECURE VERIFICATION TOKENS
-- Random, signed tokens for public verification without exposing PII in QR code
CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token VARCHAR(128) NOT NULL UNIQUE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    enrolment_id UUID NOT NULL REFERENCES enrolments(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    access_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_verification_token ON verification_tokens(token);

-- 8. AUDIT LOGS
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(100),
    institution_id UUID REFERENCES institutions(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,              -- e.g. BULK_UPLOAD, VIEW_STUDENT, VALIDATION_RUN, PUBLIC_VERIFY
    entity_type VARCHAR(50),                   -- STUDENT, ENROLMENT, INTAKE, REPORT
    entity_id VARCHAR(100),
    ip_address VARCHAR(50),
    user_agent TEXT,
    details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_institution ON audit_logs(institution_id);
