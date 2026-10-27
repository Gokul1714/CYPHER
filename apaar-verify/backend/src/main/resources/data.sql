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
