DROP DATABASE IF EXISTS OMR_AUTO;
CREATE DATABASE OMR_AUTO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE OMR_AUTO;

-- ---------- USERS ----------
CREATE TABLE users (
                       user_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255),
                       full_name VARCHAR(100),
                       avatar_url VARCHAR(255),
                       role ENUM('admin', 'teacher', 'student') NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       is_deleted BOOLEAN DEFAULT FALSE
);

-- ---------- CLASSES ----------
CREATE TABLE classes (
                         class_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         class_code VARCHAR(50) NOT NULL UNIQUE,
                         class_name VARCHAR(255) NOT NULL,
                         teacher_id INT UNSIGNED NOT NULL,
                         description TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

-- ---------- CLASS MEMBERS ----------
CREATE TABLE class_members (
                               class_member_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               class_id BIGINT UNSIGNED NOT NULL,
                               student_id INT UNSIGNED NOT NULL,
                               joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               UNIQUE KEY ux_class_student (class_id, student_id),
                               FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
                               FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------- TESTS ----------
CREATE TABLE tests (
                       test_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       test_code VARCHAR(50) NOT NULL UNIQUE,
                       title VARCHAR(255) NOT NULL,
                       subject VARCHAR(100),
                       total_questions INT NOT NULL,
                       class_id BIGINT UNSIGNED,
                       created_by INT UNSIGNED,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       description TEXT,
                       FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE SET NULL,
                       FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ---------- TEST VERSIONS ----------
CREATE TABLE test_versions (
                               version_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               test_id BIGINT UNSIGNED NOT NULL,
                               version_code VARCHAR(50) NOT NULL,
                               is_answer_key BOOLEAN NOT NULL DEFAULT FALSE,
                               file_path VARCHAR(512),
                               uploaded_by INT UNSIGNED,
                               uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE CASCADE,
                               FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE SET NULL,
                               UNIQUE KEY ux_test_version (test_id, version_code)
);

-- ---------- QUESTIONS ----------
CREATE TABLE questions (
                           question_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           version_id BIGINT UNSIGNED NOT NULL,
                           question_number INT NOT NULL,
                           question_text TEXT,
                           max_score FLOAT DEFAULT 1,
                           bubble_meta JSON,
                           FOREIGN KEY (version_id) REFERENCES test_versions(version_id) ON DELETE CASCADE,
                           UNIQUE KEY ux_version_qnum (version_id, question_number)
);

-- ---------- OPTIONS ----------
CREATE TABLE options (
                         option_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         question_id BIGINT UNSIGNED NOT NULL,
                         option_label CHAR(1) NOT NULL,
                         option_text TEXT,
                         UNIQUE KEY ux_q_option_label (question_id, option_label),
                         FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- ---------- ANSWER KEYS ----------
CREATE TABLE answer_keys (
                             answer_key_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                             question_id BIGINT UNSIGNED NOT NULL,
                             correct_option CHAR(1) NOT NULL,
                             explanation TEXT,
                             FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- ---------- OMR SHEETS ----------
CREATE TABLE omr_sheets (
                            sheet_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                            test_id BIGINT UNSIGNED NOT NULL,
                            version_id BIGINT UNSIGNED NOT NULL,
                            student_id INT UNSIGNED NOT NULL,
                            image_path VARCHAR(512) NOT NULL,
                            uploaded_by INT UNSIGNED,
                            uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            processed BOOLEAN DEFAULT FALSE,
                            processing_time TIMESTAMP NULL,
                            notes TEXT,
                            FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE CASCADE,
                            FOREIGN KEY (version_id) REFERENCES test_versions(version_id) ON DELETE RESTRICT,
                            FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                            FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE SET NULL,
                            INDEX idx_sheet_student_test (student_id, test_id)
);

-- ---------- OMR RESPONSES ----------
CREATE TABLE omr_responses (
                               response_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               sheet_id BIGINT UNSIGNED NOT NULL,
                               question_id BIGINT UNSIGNED NOT NULL,
                               detected_option CHAR(1),
                               confidence FLOAT DEFAULT 0,
                               raw_coords JSON,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (sheet_id) REFERENCES omr_sheets(sheet_id) ON DELETE CASCADE,
                               FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
                               UNIQUE KEY ux_sheet_question (sheet_id, question_id)
);

-- ---------- GRADED ITEMS ----------
CREATE TABLE graded_items (
                              graded_item_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              sheet_id BIGINT UNSIGNED NOT NULL,
                              question_id BIGINT UNSIGNED NOT NULL,
                              detected_option CHAR(1),
                              correct_option CHAR(1),
                              is_correct BOOLEAN NOT NULL DEFAULT FALSE,
                              score_awarded FLOAT DEFAULT 0,
                              max_score FLOAT DEFAULT 0,
                              grader_id INT UNSIGNED,
                              graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              feedback TEXT,
                              FOREIGN KEY (sheet_id) REFERENCES omr_sheets(sheet_id) ON DELETE CASCADE,
                              FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
                              FOREIGN KEY (grader_id) REFERENCES users(user_id) ON DELETE SET NULL,
                              UNIQUE KEY ux_graded_sheet_question (sheet_id, question_id)
);

-- ---------- RESULTS ----------
CREATE TABLE results (
                         result_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         sheet_id BIGINT UNSIGNED NOT NULL UNIQUE,
                         student_id INT UNSIGNED NOT NULL,
                         test_id BIGINT UNSIGNED NOT NULL,
                         total_correct INT DEFAULT 0,
                         total_wrong INT DEFAULT 0,
                         total_blank INT DEFAULT 0,
                         total_score FLOAT DEFAULT 0,
                         max_score FLOAT DEFAULT 0,
                         graded_by INT UNSIGNED,
                         graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         published BOOLEAN DEFAULT FALSE,
                         FOREIGN KEY (sheet_id) REFERENCES omr_sheets(sheet_id) ON DELETE CASCADE,
                         FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                         FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE CASCADE,
                         FOREIGN KEY (graded_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ---------- AUDIT LOGS ----------
CREATE TABLE audit_logs (
                            log_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                            actor_id INT UNSIGNED,
                            action_type VARCHAR(100),
                            reference_table VARCHAR(100),
                            reference_id BIGINT UNSIGNED,
                            detail TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (actor_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ---------- PASSWORD RESET TOKEN ----------
CREATE TABLE password_reset_token (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    expiry_date DATETIME NOT NULL,
    CONSTRAINT FK_password_reset_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------- DEFAULT ADMIN ----------
INSERT INTO users (username, email, password, full_name, avatar_url, role, is_deleted)
VALUES
('admin','admin@gmail.com','admin123','Administrator','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTe7mWcFQ-e97tkOw1TN2252vi4gLuYMTbvXQ&s','admin',FALSE),
('teacher01', 'teacher01@example.com', 'teach123', 'Trần Văn Giáo','/img/user.png', 'teacher',FALSE),
('teacher02', 'teacher02@example.com', 'teach456', 'Phạm Thị Dạy','/img/user.png', 'teacher',FALSE),
('student01', 'student01@example.com', 'stu123', 'Ngô Minh Học','/img/user.png', 'student',FALSE),
('student02', 'student02@example.com', 'stu456', 'Lê Thu Học', '/img/user.png', 'student',FALSE),
('student03', 'student03@example.com', 'stu789', 'Đỗ Văn Thi', '/img/user.png', 'student',FALSE),
('student04', 'student04@example.com', 'stu04', 'Phan Thanh An', '/img/user.png', 'student', FALSE);

INSERT INTO classes (class_code, class_name, teacher_id, description)
VALUES
    ('TOAN10A1', '10A1', 2, 'Lớp ban A – Toán Lý Hóa'),
    ('TOAN10A2', '10A2', 3, 'Lớp ban B – Toán Sinh Hóa');

INSERT INTO class_members (class_id, student_id)
VALUES
    (1, 4), -- student01 in 10A1
    (1, 5), -- student02 in 10A1
    (2, 6), -- student03 in 10A2
    (2, 7); -- student04 in 10A2

-- ---------- TESTS ----------
INSERT INTO tests (test_code, title, subject, total_questions, class_id, created_by, description)
VALUES
    ('MATH_HK1_10', 'Kiểm tra 15 phút Toán Học Kỳ 1', 'Toán', 5, 1, 2, 'Đề kiểm tra 15 phút chương 1 Đại số lớp 10'),
    ('BIO_HK1_10', 'Kiểm tra 15 phút Sinh Học Kỳ 1', 'Sinh Học', 5, 2, 3, 'Đề kiểm tra 15 phút chương 1 Sinh học lớp 10');

-- ---------- TEST VERSIONS ----------
INSERT INTO test_versions (test_id, version_code, is_answer_key, file_path, uploaded_by)
VALUES
    -- Math Test Versions
    (1, 'M101_KEY', TRUE, '/files/tests/math_test_1_M101_KEY.pdf', 2), -- Version with answers
    (1, 'M101_TEST', FALSE, '/files/tests/math_test_1_M101_TEST.pdf', 2), -- Version for students
    (1, 'M102_KEY', TRUE, '/files/tests/math_test_1_M102_KEY.pdf', 2), -- Another version with answers
    (1, 'M102_TEST', FALSE, '/files/tests/math_test_1_M102_TEST.pdf', 2), -- Another version for students
    -- Biology Test Versions
    (2, 'B201_KEY', TRUE, '/files/tests/bio_test_1_B201_KEY.pdf', 3),
    (2, 'B201_TEST', FALSE, '/files/tests/bio_test_1_B201_TEST.pdf', 3);

-- ---------- QUESTIONS ----------
-- Questions for Math Test (Version M101)
INSERT INTO questions (version_id, question_number, question_text, max_score)
VALUES
    (1, 1, '1 + 1 bằng mấy?', 1),
    (1, 2, '2 * 3 bằng mấy?', 1),
    (1, 3, '5 - 2 bằng mấy?', 1),
    (1, 4, '8 / 4 bằng mấy?', 1),
    (1, 5, '3^2 bằng mấy?', 1);

-- Questions for Math Test (Version M102)
INSERT INTO questions (version_id, question_number, question_text, max_score)
VALUES
    (3, 1, '2 + 2 bằng mấy?', 1),
    (3, 2, '3 * 4 bằng mấy?', 1),
    (3, 3, '10 - 5 bằng mấy?', 1),
    (3, 4, '12 / 3 bằng mấy?', 1),
    (3, 5, '4^2 bằng mấy?', 1);

-- Questions for Biology Test (Version B201)
INSERT INTO questions (version_id, question_number, question_text, max_score)
VALUES
    (5, 1, 'Nhà máy năng lượng của tế bào là gì?', 1),
    (5, 2, 'Quang hợp là gì?', 1),
    (5, 3, 'Khí nào cần thiết cho hô hấp?', 1),
    (5, 4, 'Tim người có bao nhiêu ngăn?', 1),
    (5, 5, 'Cơ quan lớn nhất trong cơ thể người là gì?', 1);

-- ---------- OPTIONS ----------
-- Options for Math Test M101
INSERT INTO options (question_id, option_label, option_text) VALUES (1, 'A', '1'), (1, 'B', '2'), (1, 'C', '3'), (1, 'D', '4');
INSERT INTO options (question_id, option_label, option_text) VALUES (2, 'A', '4'), (2, 'B', '5'), (2, 'C', '6'), (2, 'D', '7');
INSERT INTO options (question_id, option_label, option_text) VALUES (3, 'A', '1'), (3, 'B', '2'), (3, 'C', '3'), (3, 'D', '4');
INSERT INTO options (question_id, option_label, option_text) VALUES (4, 'A', '2'), (4, 'B', '3'), (4, 'C', '4'), (4, 'D', '5');
INSERT INTO options (question_id, option_label, option_text) VALUES (5, 'A', '3'), (5, 'B', '6'), (5, 'C', '9'), (5, 'D', '12');
-- Options for Math Test M102
INSERT INTO options (question_id, option_label, option_text) VALUES (6, 'A', '3'), (6, 'B', '4'), (6, 'C', '5'), (6, 'D', '6');
INSERT INTO options (question_id, option_label, option_text) VALUES (7, 'A', '10'), (7, 'B', '11'), (7, 'C', '12'), (7, 'D', '13');
INSERT INTO options (question_id, option_label, option_text) VALUES (8, 'A', '3'), (8, 'B', '4'), (8, 'C', '5'), (8, 'D', '6');
INSERT INTO options (question_id, option_label, option_text) VALUES (9, 'A', '2'), (9, 'B', '3'), (9, 'C', '4'), (9, 'D', '5');
INSERT INTO options (question_id, option_label, option_text) VALUES (10, 'A', '8'), (10, 'B', '12'), (10, 'C', '16'), (10, 'D', '20');
-- Options for Biology Test B201
INSERT INTO options (question_id, option_label, option_text) VALUES (11, 'A', 'Nhân'), (11, 'B', 'Ribosome'), (11, 'C', 'Ty thể'), (11, 'D', 'Bộ máy Golgi');
INSERT INTO options (question_id, option_label, option_text) VALUES (12, 'A', 'Quá trình tạo ra thức ăn ở thực vật'), (12, 'B', 'Quá trình hô hấp'), (12, 'C', 'Quá trình bài tiết'), (12, 'D', 'Quá trình sinh sản');
INSERT INTO options (question_id, option_label, option_text) VALUES (13, 'A', 'Oxy'), (13, 'B', 'Carbon Dioxide'), (13, 'C', 'Nitơ'), (13, 'D', 'Hydro');
INSERT INTO options (question_id, option_label, option_text) VALUES (14, 'A', '1'), (14, 'B', '2'), (14, 'C', '3'), (14, 'D', '4');
INSERT INTO options (question_id, option_label, option_text) VALUES (15, 'A', 'Gan'), (15, 'B', 'Não'), (15, 'C', 'Da'), (15, 'D', 'Tim');

-- ---------- ANSWER KEYS ----------
-- Answers for Math Test M101
INSERT INTO answer_keys (question_id, correct_option, explanation) VALUES (1, 'B', '1 + 1 = 2'), (2, 'C', '2 * 3 = 6'), (3, 'C', '5 - 2 = 3'), (4, 'A', '8 / 4 = 2'), (5, 'C', '3^2 = 9');
-- Answers for Math Test M102
INSERT INTO answer_keys (question_id, correct_option, explanation) VALUES (6, 'B', '2 + 2 = 4'), (7, 'C', '3 * 4 = 12'), (8, 'C', '10 - 5 = 5'), (9, 'C', '12 / 3 = 4'), (10, 'C', '4^2 = 16');
-- Answers for Biology Test B201
INSERT INTO answer_keys (question_id, correct_option, explanation) VALUES (11, 'C', 'Ty thể là nhà máy năng lượng của tế bào.'), (12, 'A', 'Quang hợp là quá trình thực vật sử dụng ánh sáng mặt trời để tạo ra thức ăn.'), (13, 'A', 'Oxy là khí cần thiết cho quá trình hô hấp.'), (14, 'D', 'Tim người có 4 ngăn.'), (15, 'C', 'Da là cơ quan lớn nhất của cơ thể.');

-- ---------- OMR SHEETS ----------
-- student01 (id 4) takes Math Test (id 1), version M101 (id 1)
INSERT INTO omr_sheets (test_id, version_id, student_id, image_path, uploaded_by, processed) VALUES (1, 1, 4, '/files/sheets/student01_math_test_M101.jpg', 4, TRUE);
-- student02 (id 5) takes Math Test (id 1), version M101 (id 1)
INSERT INTO omr_sheets (test_id, version_id, student_id, image_path, uploaded_by, processed) VALUES (1, 1, 5, '/files/sheets/student02_math_test_M101.jpg', 5, TRUE);
-- student03 (id 6) takes Biology Test (id 2), version B201 (id 5)
INSERT INTO omr_sheets (test_id, version_id, student_id, image_path, uploaded_by, processed) VALUES (2, 5, 6, '/files/sheets/student03_bio_test_B201.jpg', 6, TRUE);
-- student04 (id 7) takes Math Test (id 1), version M102 (id 3)
INSERT INTO omr_sheets (test_id, version_id, student_id, image_path, uploaded_by, processed) VALUES (1, 3, 7, '/files/sheets/student04_math_test_M102.jpg', 7, TRUE);

-- ---------- OMR RESPONSES ----------
-- Responses for student01 (sheet 1) - Math M101 - All correct
INSERT INTO omr_responses (sheet_id, question_id, detected_option) VALUES (1, 1, 'B'), (1, 2, 'C'), (1, 3, 'C'), (1, 4, 'A'), (1, 5, 'C');
-- Responses for student02 (sheet 2) - Math M101 - 2 correct, 3 wrong
INSERT INTO omr_responses (sheet_id, question_id, detected_option) VALUES (2, 1, 'B'), (2, 2, 'B'), (2, 3, 'C'), (2, 4, 'D'), (2, 5, 'A');
-- Responses for student03 (sheet 3) - Bio B201 - 4 correct, 1 wrong
INSERT INTO omr_responses (sheet_id, question_id, detected_option) VALUES (3, 11, 'C'), (3, 12, 'A'), (3, 13, 'A'), (3, 14, 'D'), (3, 15, 'B');
-- Responses for student04 (sheet 4) - Math M102 - 3 correct, 2 wrong
INSERT INTO omr_responses (sheet_id, question_id, detected_option) VALUES (4, 6, 'B'), (4, 7, 'C'), (4, 8, 'A'), (4, 9, 'C'), (4, 10, 'D');

-- ---------- GRADED ITEMS ----------
-- For student01 (sheet 1)
INSERT INTO graded_items (sheet_id, question_id, detected_option, correct_option, is_correct, score_awarded, max_score, grader_id) VALUES (1, 1, 'B', 'B', TRUE, 1, 1, 2), (1, 2, 'C', 'C', TRUE, 1, 1, 2), (1, 3, 'C', 'C', TRUE, 1, 1, 2), (1, 4, 'A', 'A', TRUE, 1, 1, 2), (1, 5, 'C', 'C', TRUE, 1, 1, 2);
-- For student02 (sheet 2)
INSERT INTO graded_items (sheet_id, question_id, detected_option, correct_option, is_correct, score_awarded, max_score, grader_id) VALUES (2, 1, 'B', 'B', TRUE, 1, 1, 2), (2, 2, 'B', 'C', FALSE, 0, 1, 2), (2, 3, 'C', 'C', TRUE, 1, 1, 2), (2, 4, 'D', 'A', FALSE, 0, 1, 2), (2, 5, 'A', 'C', FALSE, 0, 1, 2);
-- For student03 (sheet 3)
INSERT INTO graded_items (sheet_id, question_id, detected_option, correct_option, is_correct, score_awarded, max_score, grader_id) VALUES (3, 11, 'C', 'C', TRUE, 1, 1, 3), (3, 12, 'A', 'A', TRUE, 1, 1, 3), (3, 13, 'A', 'A', TRUE, 1, 1, 3), (3, 14, 'D', 'D', TRUE, 1, 1, 3), (3, 15, 'B', 'C', FALSE, 0, 1, 3);
-- For student04 (sheet 4)
INSERT INTO graded_items (sheet_id, question_id, detected_option, correct_option, is_correct, score_awarded, max_score, grader_id) VALUES (4, 6, 'B', 'B', TRUE, 1, 1, 2), (4, 7, 'C', 'C', TRUE, 1, 1, 2), (4, 8, 'A', 'C', FALSE, 0, 1, 2), (4, 9, 'C', 'C', TRUE, 1, 1, 2), (4, 10, 'D', 'C', FALSE, 0, 1, 2);

-- ---------- RESULTS ----------
INSERT INTO results (sheet_id, student_id, test_id, total_correct, total_wrong, total_blank, total_score, max_score, graded_by, published) VALUES (1, 4, 1, 5, 0, 0, 5, 5, 2, TRUE);
INSERT INTO results (sheet_id, student_id, test_id, total_correct, total_wrong, total_blank, total_score, max_score, graded_by, published) VALUES (2, 5, 1, 2, 3, 0, 2, 5, 2, TRUE);
INSERT INTO results (sheet_id, student_id, test_id, total_correct, total_wrong, total_blank, total_score, max_score, graded_by, published) VALUES (3, 6, 2, 4, 1, 0, 4, 5, 3, TRUE);
INSERT INTO results (sheet_id, student_id, test_id, total_correct, total_wrong, total_blank, total_score, max_score, graded_by, published) VALUES (4, 7, 1, 3, 2, 0, 3, 5, 2, TRUE);

-- Thêm cột status và is_hidden cho bảng class_members nếu chưa có
ALTER TABLE class_members ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'approved';
ALTER TABLE class_members ADD COLUMN is_hidden BOOLEAN NOT NULL DEFAULT FALSE;

-- Đảm bảo các lớp đã tham gia đều có status = 'approved' và is_hidden = false
-- (Bạn có thể sửa điều kiện WHERE cho phù hợp với từng học sinh hoặc từng lớp)
UPDATE class_members SET status = 'approved' WHERE status IS NULL OR status = '';
UPDATE class_members SET is_hidden = false WHERE is_hidden IS NULL;

