-- Add test version with code "125" for OMR testing
USE OMR_AUTO;

-- First, check if we have a test to associate with
-- If not, create a generic test
INSERT IGNORE INTO tests (subject_id, test_code, title, total_questions, created_by, description)
VALUES
    (1, 'TEST_125', 'Test with code 125', 20, 2, 'Generic test for code 125');

-- Get the test_id (assuming it's the one we just created or already exists)
SET @test_id = (SELECT test_id FROM tests WHERE test_code = 'TEST_125' LIMIT 1);

-- Add the test version with code "125"
INSERT INTO test_versions (test_id, version_code, pdf_path, csv_answer_path, uploaded_by, answer_path)
VALUES
    (@test_id, '125', '/files/tests/test_125.pdf', '/files/answers/test_125.csv', 2, 'py/answer_keys/MATH10_HK1_001.json')
ON DUPLICATE KEY UPDATE
    version_code = '125',
    answer_path = 'py/answer_keys/MATH10_HK1_001.json';

-- Verify
SELECT tv.version_id, tv.version_code, t.test_code, t.title, tv.answer_path
FROM test_versions tv
         JOIN tests t ON tv.test_id = t.test_id
WHERE tv.version_code = '125';
