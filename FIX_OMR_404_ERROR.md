# OMR Error Fix Guide - Test Version 125 Not Found

## Problem
The OMR system returned a 404 error:
```
❌ Error data: {success: false, message: 'Test version not found: 125'}
```

This happens because:
1. The OMR scanner detected test code "125" from the answer sheet
2. The database doesn't have a test version with code "125"
3. The system cannot proceed without matching the test version

## Solution Steps

### Step 1: Run the SQL Script
Execute the SQL script to add the missing test version and student:

```bash
# In MySQL Workbench or command line:
mysql -u root -p < fix_omr_database.sql
```

Or manually run the SQL commands:

```sql
USE OMR_AUTO;

-- 1. Add Test
INSERT IGNORE INTO tests (subject_id, test_code, title, total_questions, created_by, description)
VALUES (1, 'TEST_125', 'Test Mã Đề 125', 20, 2, 'Đề thi mã 125 - 20 câu trắc nghiệm');

-- 2. Get test_id
SET @test_id = (SELECT test_id FROM tests WHERE test_code = 'TEST_125' LIMIT 1);

-- 3. Add Test Version
DELETE FROM test_versions WHERE version_code = '125';
INSERT INTO test_versions (test_id, version_code, pdf_path, csv_answer_path, uploaded_by, answer_path, is_answer_key)
VALUES (@test_id, '125', '/files/tests/test_125.pdf', NULL, 2, 'py/answer_keys/test_125.json', TRUE);

-- 4. Add Student
INSERT IGNORE INTO users (id_omr, username, email, password, full_name, role)
VALUES ('036537', 'student036537', 'student036537@example.com', '$2a$10$dummyhash', 'Học sinh 036537', 'student');

-- 5. Verify
SELECT tv.version_code, t.test_code, t.title, tv.answer_path
FROM test_versions tv
JOIN tests t ON tv.test_id = t.test_id
WHERE tv.version_code = '125';
```

### Step 2: Verify Answer Key File
The answer key file has been created at:
- **Path**: `py/answer_keys/test_125.json`
- **Content**: 20 questions with answers A-E

### Step 3: Restart Spring Boot Application
After updating the database, restart your Spring Boot application:

**Option A: IntelliJ IDEA**
1. Stop the running application (Red square button)
2. Restart (Green play button)

**Option B: Command Line**
```bash
cd "d:\ki 7\DAT301mK"
.\mvnw.cmd spring-boot:run
```

### Step 4: Test Again
1. Open the OMR scanning page
2. Upload the same image (with test code 125 and student ID 036537)
3. The system should now process successfully

## What Was Fixed

### ✅ Database
- Added test: `TEST_125` with 20 questions
- Added test version: `125` with answer key path
- Added student: `036537` (SBD from the OMR sheet)
- Added additional versions: 101-103, 124, 126-127 for testing

### ✅ Answer Key
- Created `py/answer_keys/test_125.json` with correct answers
- Matches the ANSWER_KEY in `2.py`

### ✅ Configuration
- Previously fixed: `python.script.path=py/omr_cli.py` (was pointing to wrong file)

## Expected Result

After applying the fix, when you submit an OMR sheet with test code "125":

```json
{
  "success": true,
  "message": "OMR sheet processed successfully",
  "data": {
    "studentId": "036537",
    "studentName": "Học sinh 036537",
    "testCode": "125",
    "testName": "Test Mã Đề 125",
    "totalQuestions": 20,
    "correctAnswers": 6,
    "score": 3.0,
    "resultId": 1,
    "outputImage": "runs\\predict\\omr_sample\\graded_20251106_045955.jpg",
    "questions": [...]
  }
}
```

## Troubleshooting

### Error: "Student not found with ID: 036537"
Run the student insert SQL:
```sql
INSERT IGNORE INTO users (id_omr, username, email, password, full_name, role)
VALUES ('036537', 'student036537', 'student036537@example.com', '$2a$10$dummyhash', 'Học sinh 036537', 'student');
```

### Error: "Test version not found: [OTHER_CODE]"
For any other test code, add it using the same pattern:
```sql
SET @test_id = (SELECT test_id FROM tests WHERE test_code = 'TEST_125' LIMIT 1);
INSERT INTO test_versions (test_id, version_code, pdf_path, uploaded_by, answer_path)
VALUES (@test_id, '[YOUR_CODE]', '/files/tests/test_[YOUR_CODE].pdf', 2, 'py/answer_keys/test_125.json');
```

### Error: Still getting 500 error
1. Check if Spring Boot picked up the config change
2. Look at Spring Boot logs for the actual error
3. Verify Python script is executable: `python py/omr_cli.py "uploads/omr/[FILE].jpg" 0.35`

## Files Created/Modified

### New Files
- ✅ `fix_omr_database.sql` - Complete database setup script
- ✅ `py/answer_keys/test_125.json` - Answer key for test version 125
- ✅ `add_test_version_125.sql` - Individual test version script
- ✅ `add_student_036537.sql` - Individual student script

### Modified Files
- ✅ `src/main/resources/application.properties` - Fixed Python script path
- ✅ `target/classes/application.properties` - Fixed Python script path

## Quick Reference

### Test Version 125 Details
- **Test Code**: TEST_125
- **Version Code**: 125
- **Total Questions**: 20
- **Answer Key**: py/answer_keys/test_125.json
- **Scoring**: 0.5 points per question (total 10 points)

### Student 036537 Details
- **ID (SBD)**: 036537
- **Username**: student036537
- **Role**: student
- **Full Name**: Học sinh 036537

## Next Steps

1. ✅ Run `fix_omr_database.sql` in MySQL
2. ✅ Restart Spring Boot application
3. ✅ Test with the OMR image
4. 📝 Add more test versions as needed
5. 📝 Configure dynamic test version creation if needed

