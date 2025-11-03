DROP DATABASE IF EXISTS OMR_AUTO;
CREATE DATABASE OMR_AUTO ;
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

-- =======================================================
-- SUBJECTS
-- =======================================================
CREATE TABLE subjects (
                          subject_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          subject_code VARCHAR(50) NOT NULL UNIQUE,
                          subject_name VARCHAR(100) NOT NULL,
                          description TEXT
);

-- =======================================================
-- CLASSES
-- =======================================================
CREATE TABLE classes (
                         class_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         class_code VARCHAR(50) NOT NULL UNIQUE,
                         class_name VARCHAR(255) NOT NULL,
                         teacher_id INT UNSIGNED NOT NULL,
                         description TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

-- =======================================================
-- CLASS MEMBERS
-- =======================================================
CREATE TABLE class_members (
                               class_member_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               class_id BIGINT UNSIGNED NOT NULL,
                               student_id INT UNSIGNED NOT NULL,
                               joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               status VARCHAR(20) NOT NULL DEFAULT 'approved',
                               is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
                               UNIQUE KEY ux_class_student (class_id, student_id),
                               FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
                               FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =======================================================
-- TESTS (ĐỀ THI GỐC)
-- =======================================================
CREATE TABLE tests (
                       test_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       subject_id INT UNSIGNED NOT NULL,
                       test_code VARCHAR(50) NOT NULL UNIQUE,
                       title VARCHAR(255) NOT NULL,
                       total_questions INT DEFAULT NULL,
                       created_by INT UNSIGNED NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       description TEXT,
                       FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
                       FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =======================================================
-- TEST VERSIONS (MÃ ĐỀ)
-- =======================================================
CREATE TABLE test_versions (
                               version_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               test_id BIGINT UNSIGNED NOT NULL,
                               version_code VARCHAR(100) NOT NULL,
                               pdf_path VARCHAR(512) NOT NULL,
                               csv_answer_path VARCHAR(512),
                               is_answer_key BOOLEAN DEFAULT FALSE,
                               uploaded_by INT UNSIGNED NOT NULL,
                               uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               description TEXT,
                               FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE CASCADE,
                               FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE CASCADE,
                               UNIQUE KEY ux_test_version (test_id, version_code)
);

-- =======================================================
-- CLASS TESTS (GÁN ĐỀ CHO LỚP)
-- =======================================================
CREATE TABLE class_tests (
                             class_test_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                             class_id BIGINT UNSIGNED NOT NULL,
                             test_id BIGINT UNSIGNED NOT NULL,
                             assigned_by INT UNSIGNED NOT NULL,
                             scheduled_date DATETIME,
                             note TEXT,
                             FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
                             FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE CASCADE,
                             FOREIGN KEY (assigned_by) REFERENCES users(user_id) ON DELETE CASCADE,
                             UNIQUE KEY ux_class_test (class_id, test_id)
);

-- =======================================================
-- OMR SHEETS (BÀI LÀM)
-- =======================================================
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

-- =======================================================
-- GRADED ITEMS (KẾT QUẢ CHI TIẾT)
-- =======================================================
CREATE TABLE graded_items (
                              graded_item_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              sheet_id BIGINT UNSIGNED NOT NULL,
                              question_number INT NOT NULL,
                              detected_option CHAR(1),
                              correct_option CHAR(1),
                              is_correct BOOLEAN NOT NULL DEFAULT FALSE,
                              score_awarded FLOAT DEFAULT 0,
                              max_score FLOAT DEFAULT 1,
                              grader_id INT UNSIGNED,
                              graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              feedback TEXT,
                              FOREIGN KEY (sheet_id) REFERENCES omr_sheets(sheet_id) ON DELETE CASCADE,
                              FOREIGN KEY (grader_id) REFERENCES users(user_id) ON DELETE SET NULL,
                              UNIQUE KEY ux_graded_sheet_question (sheet_id, question_number)
);

-- =======================================================
-- RESULTS (TỔNG KẾT)
-- =======================================================
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

-- =======================================================
-- AUDIT LOGS
-- =======================================================
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

-- =======================================================
-- PASSWORD RESET TOKENS
-- =======================================================
CREATE TABLE password_reset_token (
                                      id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                      token VARCHAR(255) NOT NULL,
                                      user_id INT UNSIGNED NOT NULL,
                                      expiry_date DATETIME NOT NULL,
                                      FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =======================================================
-- SAMPLE DATA
-- =======================================================

-- USERS
INSERT INTO users (username, email, password, full_name, avatar_url, role)
VALUES
    ('admin','admin@gmail.com','admin123','Administrator','/img/admin.png','admin'),
    ('teacher01','teacher01@example.com','teach123','Nguyễn Văn Toán','/img/teacher.png','teacher'),
    ('teacher02','teacher02@example.com','teach456','Phạm Thị Sinh','/img/teacher.png','teacher'),
    ('student01','student01@example.com','stu123','Ngô Minh Học','/img/student.png','student'),
    ('student02','student02@example.com','stu456','Lê Thu Học','/img/student.png','student');

-- SUBJECTS
INSERT INTO subjects (subject_code, subject_name, description)
VALUES
    ('MATH10','Toán học lớp 10','Môn Toán lớp 10 - Đại số & Hình học'),
    ('BIO10','Sinh học lớp 10','Môn Sinh học lớp 10 - Cấu trúc tế bào');

-- CLASSES
INSERT INTO classes (class_code, class_name, teacher_id, description)
VALUES
    ('TOAN10A1','Lớp 10A1',2,'Lớp ban A – Toán Lý Hóa'),
    ('TOAN10A2','Lớp 10A2',3,'Lớp ban B – Toán Sinh Hóa');

-- CLASS MEMBERS
INSERT INTO class_members (class_id, student_id)
VALUES
    (1,4),(1,5);

-- TESTS
INSERT INTO tests (subject_id, test_code, title, total_questions, created_by, description)
VALUES
    (1,'MATH10_HK1','Kiểm tra Học kỳ 1 - Toán 10',5,2,'Đề HK1 chương 1-3'),
    (2,'BIO10_HK1','Kiểm tra Học kỳ 1 - Sinh học 10',5,3,'Đề HK1 chương tế bào');

-- TEST VERSIONS
INSERT INTO test_versions (test_id, version_code, pdf_path, csv_answer_path, uploaded_by)
VALUES
    (1,'MATH10_HK1_001','/files/tests/MATH10_HK1_001.pdf','/files/answers/MATH10_HK1_001.csv',2),
    (1,'MATH10_HK1_002','/files/tests/MATH10_HK1_002.pdf','/files/answers/MATH10_HK1_002.csv',2),
    (2,'BIO10_HK1_001','/files/tests/BIO10_HK1_001.pdf','/files/answers/BIO10_HK1_001.csv',3);

-- CLASS TESTS
INSERT INTO class_tests (class_id, test_id, assigned_by, scheduled_date)
VALUES
    (1,1,2,'2025-11-10 08:00:00'),
    (2,2,3,'2025-11-11 08:00:00');

-- SAMPLE OMR SHEETS
INSERT INTO omr_sheets (test_id, version_id, student_id, image_path, uploaded_by, processed)
VALUES
    (1,1,4,'/files/sheets/student01_MATH10_HK1_001.jpg',4,TRUE),
    (1,2,5,'/files/sheets/student02_MATH10_HK1_002.jpg',5,TRUE);

-- GRADED ITEMS
INSERT INTO graded_items (sheet_id, question_number, detected_option, correct_option, is_correct, score_awarded, grader_id)
VALUES
    (1,1,'B','B',TRUE,1,2),(1,2,'C','C',TRUE,1,2),(1,3,'A','A',TRUE,1,2),(1,4,'D','D',TRUE,1,2),(1,5,'C','C',TRUE,1,2),
    (2,1,'A','B',FALSE,0,2),(2,2,'C','C',TRUE,1,2),(2,3,'A','A',TRUE,1,2),(2,4,'D','B',FALSE,0,2),(2,5,'C','C',TRUE,1,2);

-- RESULTS
INSERT INTO results (sheet_id, student_id, test_id, total_correct, total_wrong, total_blank, total_score, max_score, graded_by, published)
VALUES
    (1,4,1,5,0,0,5,5,2,TRUE),
    (2,5,1,3,2,0,3,5,2,TRUE);
ALTER TABLE test_versions ADD COLUMN file_path VARCHAR(512);
ALTER TABLE test_versions ADD COLUMN answer_path VARCHAR(512);
