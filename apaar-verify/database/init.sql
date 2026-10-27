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


-- =============================================================================
-- CYPHER: Seed Data (Synthetic & AICTE Demonstration Records)
-- All student data is purely synthetic. Passwords are set for development: 'password123'
-- =============================================================================

-- 1. SEED INSTITUTIONS
INSERT INTO institutions (id, aicte_code, name, state, district, institution_type, contact_email, contact_phone)
VALUES
('11111111-1111-1111-1111-111111111111', '1-AICTE-DELHI-001', 'Delhi Institute of Engineering & Technology', 'Delhi', 'New Delhi', 'Central University', 'admin@diet.ac.in', '+91 11 2659 1000'),
('22222222-2222-2222-2222-222222222222', '1-AICTE-MAHA-002', 'Maharashtra National Institute of Technology', 'Maharashtra', 'Pune', 'State Government', 'admin@mnit.edu.in', '+91 20 2576 2000'),
('33333333-3333-3333-3333-333333333333', '1-AICTE-KARN-003', 'Bangalore Institute of Applied Sciences', 'Karnataka', 'Bengaluru', 'Private-Self Financed', 'admin@bias.edu.in', '+91 80 2360 3000')
ON CONFLICT (aicte_code) DO NOTHING;

-- 2. SEED USERS (BCrypt hashed for 'password123': $2a$10$wE1K6Z5Y2R.MszU09hK9rO0wE9P0n/kFk2y2r2Q1wY0Y0gX8z8z8S -> placeholder)
INSERT INTO users (id, username, password_hash, full_name, email, staff_id, department, is_approved, role, institution_id)
VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'aicte_officer', '$2a$10$1yF07M0yO5l3cK3yK0qKceK7c.qWJ6I4yWwO6e0mU4n9R2jU6C7E2', 'Dr. Ramesh Kumar (Director AICTE)', 'officer@aicte-india.org', 'AICTE-HQ-001', 'Bureau of Approval', TRUE, 'ROLE_AICTE_ADMIN', NULL),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'diet_admin', '$2a$10$1yF07M0yO5l3cK3yK0qKceK7c.qWJ6I4yWwO6e0mU4n9R2jU6C7E2', 'Prof. Sunita Sharma', 'registrar@diet.ac.in', 'DIET-REG-001', 'Academic Affairs', TRUE, 'ROLE_INSTITUTION_ADMIN', '11111111-1111-1111-1111-111111111111'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'mnit_admin', '$2a$10$1yF07M0yO5l3cK3yK0qKceK7c.qWJ6I4yWwO6e0mU4n9R2jU6C7E2', 'Prof. Arvind Deshmukh', 'registrar@mnit.edu.in', 'MNIT-REG-002', 'Academic Affairs', TRUE, 'ROLE_INSTITUTION_ADMIN', '22222222-2222-2222-2222-222222222222')
ON CONFLICT (username) DO NOTHING;

-- 3. APPROVED INTAKE QUOTAS (2026-2027)
INSERT INTO approved_intake (id, institution_id, course_name, department, academic_year, approved_seats, nri_quota, supernumerary_quota, approval_ref_number)
VALUES
(uuid_generate_v4(), '11111111-1111-1111-1111-111111111111', 'B.Tech Computer Science & Engineering', 'Engineering & Technology', '2026-2027', 120, 6, 12, 'AICTE/F.No/NWRO/2026/01-120'),
(uuid_generate_v4(), '11111111-1111-1111-1111-111111111111', 'B.Tech Electronics & Communication', 'Engineering & Technology', '2026-2027', 60, 3, 6, 'AICTE/F.No/NWRO/2026/01-060'),
(uuid_generate_v4(), '22222222-2222-2222-2222-222222222222', 'B.Tech Computer Science & Engineering', 'Engineering & Technology', '2026-2027', 180, 9, 18, 'AICTE/F.No/WRO/2026/02-180'),
(uuid_generate_v4(), '22222222-2222-2222-2222-222222222222', 'B.Tech Mechanical Engineering', 'Engineering & Technology', '2026-2027', 60, 3, 6, 'AICTE/F.No/WRO/2026/02-060')
ON CONFLICT DO NOTHING;

-- 4. SYNTHETIC STUDENTS
-- Note: APAAR IDs are 12-digit numeric identifiers (e.g., 202610014521)
INSERT INTO students (id, student_name, gender, date_of_birth, apaar_id, apaar_format_valid, apaar_verification_status, father_or_guardian_name)
VALUES
('s1111111-1111-1111-1111-111111111111', 'Aarav Patel', 'MALE', '2005-04-12', '982345012344', TRUE, 'MOCK_VERIFIED', 'Rajesh Patel'),
('s2222222-2222-2222-2222-222222222222', 'Priya Iyer', 'FEMALE', '2005-08-23', '814529081239', TRUE, 'MOCK_VERIFIED', 'Srinivasan Iyer'),
('s3333333-3333-3333-3333-333333333333', 'Rohan Verma', 'MALE', '2004-11-05', '12345', FALSE, 'UNVERIFIED', 'Suresh Verma'), -- Invalid format (< 12 digits)
('s4444444-4444-4444-4444-444444444444', 'Ananya Sengupta', 'FEMALE', '2005-01-30', NULL, FALSE, 'UNVERIFIED', 'Debashis Sengupta'), -- Missing APAAR
('s5555555-5555-5555-5555-555555555555', 'Karan Johar Sharma', 'MALE', '2005-06-19', '773412095621', TRUE, 'MOCK_VERIFIED', 'Mohan Sharma'); -- Dual enrolled student across institutions

-- 5. ENROLMENTS
INSERT INTO enrolments (id, student_id, institution_id, course_name, department, academic_year, roll_number, enrolment_status, admission_category, is_nri, is_oci, is_foreign_national, is_supernumerary, admission_date)
VALUES
(uuid_generate_v4(), 's1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'B.Tech Computer Science & Engineering', 'Engineering & Technology', '2026-2027', '26-CSE-001', 'ACTIVE', 'GENERAL', FALSE, FALSE, FALSE, FALSE, '2026-07-15'),
(uuid_generate_v4(), 's2222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'B.Tech Computer Science & Engineering', 'Engineering & Technology', '2026-2027', '26-CSE-002', 'ACTIVE', 'OBC_NCL', FALSE, FALSE, FALSE, FALSE, '2026-07-16'),
(uuid_generate_v4(), 's3333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'B.Tech Computer Science & Engineering', 'Engineering & Technology', '2026-2027', '26-CSE-003', 'ACTIVE', 'SC', FALSE, FALSE, FALSE, FALSE, '2026-07-18'),
(uuid_generate_v4(), 's4444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'B.Tech Electronics & Communication', 'Engineering & Technology', '2026-2027', '26-ECE-001', 'ACTIVE', 'GENERAL', FALSE, FALSE, FALSE, FALSE, '2026-07-20'),
-- Intentional cross-institution dual active enrolment for validation demonstration:
(uuid_generate_v4(), 's5555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'B.Tech Computer Science & Engineering', 'Engineering & Technology', '2026-2027', '26-CSE-045', 'ACTIVE', 'GENERAL', FALSE, FALSE, FALSE, FALSE, '2026-07-22'),
(uuid_generate_v4(), 's5555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', 'B.Tech Computer Science & Engineering', 'Engineering & Technology', '2026-2027', '26-MNIT-112', 'ACTIVE', 'GENERAL', FALSE, FALSE, FALSE, FALSE, '2026-08-01')
ON CONFLICT DO NOTHING;

-- 6. VERIFICATION TOKENS
INSERT INTO verification_tokens (id, token, student_id, enrolment_id, expires_at, access_count)
SELECT 
    uuid_generate_v4(),
    'tok_sec_982345012344_aicte_verified_sig',
    's1111111-1111-1111-1111-111111111111',
    e.id,
    NOW() + INTERVAL '1 year',
    3
FROM enrolments e WHERE e.student_id = 's1111111-1111-1111-1111-111111111111' LIMIT 1
ON CONFLICT DO NOTHING;

-- 7. INITIAL AUDIT LOG
INSERT INTO audit_logs (id, user_id, username, institution_id, action, entity_type, entity_id, ip_address, user_agent, details)
VALUES
(uuid_generate_v4(), 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'aicte_officer', NULL, 'SYSTEM_INIT', 'PLATFORM', 'ROOT', '127.0.0.1', 'System/Boot', '{"note": "Initial schema and sample data loaded successfully"}');

