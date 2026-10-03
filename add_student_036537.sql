-- Add student with ID 036537 for OMR testing
USE OMR_AUTO;

-- Check if student exists
SELECT user_id, id_omr, username, full_name, role
FROM users
WHERE id_omr = '036537';

-- If not exists, add the student
INSERT IGNORE INTO users (id_omr, username, email, password, full_name, role)
VALUES
    ('036537', 'student036537', 'student036537@example.com', 'stu123', 'Học sinh 036537', 'student');

-- Verify
SELECT user_id, id_omr, username, full_name, role
FROM users
WHERE id_omr = '036537';
