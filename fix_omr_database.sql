-- Complete setup for OMR test version 125 and student 036537
-- Run this in MySQL to fix the 404 error

USE OMR_AUTO;

-- ========================================
-- 1. Add Test if not exists
-- ========================================
INSERT IGNORE INTO tests (subject_id, test_code, title, total_questions, created_by, description)
VALUES
    (1, 'TEST_125', 'Test Mã Đề 125', 20, 2, 'Đề thi mã 125 - 20 câu trắc nghiệm');

-- Get test_id
SET @test_id = (SELECT test_id FROM tests WHERE test_code = 'TEST_125' LIMIT 1);

-- ========================================
-- 2. Add Test Version with code "125"
-- ========================================
-- Delete if exists to avoid duplicates
DELETE FROM test_versions WHERE version_code = '125';

-- Insert new version
INSERT INTO test_versions (test_id, version_code, pdf_path, csv_answer_path, uploaded_by, answer_path, is_answer_key)
VALUES
    (@test_id, '125', '/files/tests/test_125.pdf', NULL, 2, 'py/answer_keys/test_125.json', TRUE);

-- ========================================
-- 3. Add Student with ID 036537
-- ========================================
-- Check and add if not exists
INSERT IGNORE INTO users (id_omr, username, email, password, full_name, role)
VALUES
    ('036537', 'student036537', 'student036537@example.com', '$2a$10$dummyhash', 'Học sinh Nguyễn Văn A - 036537', 'student');

-- ========================================
-- 4. Add more common test versions
-- ========================================
-- Add versions 101-130 for testing
SET @test_id = (SELECT test_id FROM tests WHERE test_code = 'TEST_125' LIMIT 1);

INSERT IGNORE INTO test_versions (test_id, version_code, pdf_path, csv_answer_path, uploaded_by, answer_path, is_answer_key)
VALUES
    (@test_id, '101', '/files/tests/test_101.pdf', NULL, 2, 'py/answer_keys/MATH10_HK1_001.json', TRUE),
    (@test_id, '102', '/files/tests/test_102.pdf', NULL, 2, 'py/answer_keys/MATH10_HK1_001.json', TRUE),
    (@test_id, '103', '/files/tests/test_103.pdf', NULL, 2, 'py/answer_keys/MATH10_HK1_001.json', TRUE),
    (@test_id, '124', '/files/tests/test_124.pdf', NULL, 2, 'py/answer_keys/MATH10_HK1_001.json', TRUE),
    (@test_id, '126', '/files/tests/test_126.pdf', NULL, 2, 'py/answer_keys/MATH10_HK1_001.json', TRUE),
    (@test_id, '127', '/files/tests/test_127.pdf', NULL, 2, 'py/answer_keys/MATH10_HK1_001.json', TRUE);

-- ========================================
-- 5. Verify the setup
-- ========================================
SELECT '=== Test Versions ===\n' AS '✓ Setup Complete';

SELECT 
    tv.version_id,
    tv.version_code AS 'Mã Đề',
    t.test_code AS 'Mã Test',
    t.title AS 'Tên Test',
    tv.answer_path AS 'Answer Key Path'
FROM test_versions tv
JOIN tests t ON tv.test_id = t.test_id
WHERE tv.version_code IN ('125', '101', '102', '103', '124', '126', '127')
ORDER BY tv.version_code;

SELECT '\n=== Students ===\n' AS '';

SELECT 
    user_id AS 'User ID',
    id_omr AS 'SBD',
    username AS 'Username',
    full_name AS 'Full Name',
    role AS 'Role'
FROM users
WHERE id_omr = '036537' OR role = 'student'
ORDER BY id_omr;

