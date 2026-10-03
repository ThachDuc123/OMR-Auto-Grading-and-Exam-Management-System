# 📝 OMR Auto-Grading System - Complete Integration Guide

## 🎯 Overview
This system automatically grades OMR (Optical Mark Recognition) sheets by:
1. Scanning uploaded images
2. Detecting student ID (id_omr) and test version code (made)
3. Comparing with answer keys
4. Saving results to database
5. Displaying scores to students

---

## 🏗️ System Architecture

### Frontend (Scan_omr.html)
- **Location**: `src/main/resources/templates/Scan_omr.html`
- **Access**: `http://localhost:8081/scan_omr`
- **Features**:
  - Image upload and preview
  - Confidence adjustment slider
  - Real-time OMR processing
  - Results display with detailed breakdown
  - Question-by-question analysis

### Backend API (OmrController.java)
- **Endpoint**: `POST /api/omr/process`
- **Location**: `src/main/java/com/example/dat301mk/controller/OmrController.java`
- **Function**: Processes uploaded OMR sheets and saves results

### Python Script (2.py)
- **Location**: `py/2.py`
- **Function**: 
  - Detects bubbles using YOLO
  - Extracts student ID (SBD) and test code (MADE)
  - Compares answers with answer key
  - Returns JSON results

---

## 📊 Database Schema

### Key Tables

#### 1. `users` - Student Information
```sql
CREATE TABLE users (
    user_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_omr CHAR(6) UNIQUE,           -- 6-digit student ID (auto-generated)
    username VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    role ENUM('admin', 'teacher', 'student')
);
```

**Auto-Generate id_omr Trigger**:
- Automatically creates 6-digit sequential ID for students
- Format: 000001, 000002, etc.

#### 2. `tests` - Test Information
```sql
CREATE TABLE tests (
    test_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    test_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    total_questions INT,
    subject_id INT UNSIGNED NOT NULL
);
```

#### 3. `test_versions` - Test Versions (Mã đề)
```sql
CREATE TABLE test_versions (
    version_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    test_id BIGINT UNSIGNED NOT NULL,
    version_code VARCHAR(100) NOT NULL,      -- E.g., "MATH10_HK1_001"
    pdf_path VARCHAR(512),
    csv_answer_path VARCHAR(512),            -- Path to answer key
    answer_path VARCHAR(512),                -- Modern answer path
    UNIQUE KEY (test_id, version_code)
);
```

#### 4. `omr_sheets` - Scanned Sheets
```sql
CREATE TABLE omr_sheets (
    sheet_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    test_id BIGINT UNSIGNED NOT NULL,
    version_id BIGINT UNSIGNED NOT NULL,
    student_id INT UNSIGNED NOT NULL,        -- Links to users.user_id
    image_path VARCHAR(512),
    processed BOOLEAN DEFAULT FALSE,
    processing_time TIMESTAMP
);
```

#### 5. `graded_items` - Detailed Question Results
```sql
CREATE TABLE graded_items (
    graded_item_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    sheet_id BIGINT UNSIGNED NOT NULL,
    question_number INT NOT NULL,
    detected_option CHAR(1),                 -- Student's answer
    correct_option CHAR(1),                  -- Correct answer
    is_correct BOOLEAN DEFAULT FALSE,
    score_awarded FLOAT DEFAULT 0
);
```

#### 6. `results` - Final Scores
```sql
CREATE TABLE results (
    result_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    sheet_id BIGINT UNSIGNED NOT NULL UNIQUE,
    student_id INT UNSIGNED NOT NULL,
    test_id BIGINT UNSIGNED NOT NULL,
    total_correct INT DEFAULT 0,
    total_wrong INT DEFAULT 0,
    total_score FLOAT DEFAULT 0,
    published BOOLEAN DEFAULT FALSE          -- If true, student can see result
);
```

---

## 🔄 Data Flow

### 1. Teacher Uploads Test Version with Answer Key
```
Teacher → /teacher_quiz → Upload PDF + CSV Answer Key
  ↓
test_versions table: 
  - version_code = "MATH10_HK1_001"
  - csv_answer_path = "/files/answers/MATH10_HK1_001.csv"
```

### 2. Student Takes Test
```
Student prints test → Fills OMR sheet → Returns to teacher
```

### 3. Teacher Scans OMR Sheet
```
Teacher → /scan_omr → Upload scanned image → Click "Chấm bài"
  ↓
POST /api/omr/process
  ↓
Python 2.py processes image
  ↓
Returns: { sbd: "000123", made: "MATH10_HK1_001", score: 8.5, ... }
  ↓
Backend saves to database:
  - omr_sheets (scanned image record)
  - graded_items (each question result)
  - results (final score)
```

### 4. Student Views Result
```
Student → /student_home → See score for completed tests
  ↓
Query: SELECT * FROM results WHERE student_id = ? AND published = true
```

---

## 🔧 Configuration

### application.properties
```properties
# OMR Configuration
omr.upload.dir=uploads/omr
python.script.path=py/2.py
python.executable=python

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Python Script Output Format
The `2.py` script must output JSON in this format:
```json
{
  "sbd": "000123",
  "made": "MATH10_HK1_001",
  "total_questions": 20,
  "correct": 15,
  "wrong": 5,
  "blank": 0,
  "score": 7.5,
  "output_image": "/path/to/annotated_image.jpg",
  "questions": [
    {
      "question": 1,
      "studentAnswer": "A",
      "correctAnswer": "A",
      "isCorrect": true
    },
    ...
  ]
}
```

---

## 🚀 Usage Workflow

### For Teachers:

#### Step 1: Upload Test & Answer Key
1. Go to `/teacher_quiz`
2. Select subject and test
3. Upload test PDF (e.g., `MATH10_HK1_001.pdf`)
4. Upload answer key CSV (e.g., `MATH10_HK1_001.csv`)
5. Click "Upload"

#### Step 2: Scan Student Sheets
1. Go to `/scan_omr`
2. Click "Choose Image" → Select scanned OMR sheet
3. Adjust confidence slider (default: 0.35)
4. Click "📝 Chấm bài" (Grade)
5. Wait for processing...
6. View results:
   - Student ID (SBD)
   - Test Code (Mã đề)
   - Score (Điểm)
   - Question breakdown

#### Step 3: Publish Results
- Results are automatically published (`published = true`)
- Students can immediately see their scores

---

### For Students:

#### View Results
1. Go to `/student_home`
2. See "Kết quả thi" section
3. View:
   - Test name
   - Score
   - Date graded
   - Status (if published)

---

## 🔍 Troubleshooting

### Issue 1: Student Not Found
**Error**: "Student not found with id_omr: 000123"

**Cause**: Python script detected ID that doesn't exist in database

**Solution**:
1. Check if student exists: `SELECT * FROM users WHERE id_omr = '000123'`
2. If not, create student or verify OMR sheet has correct ID

### Issue 2: Test Version Not Found
**Error**: "Test version not found: MATH10_HK1_001"

**Cause**: Version code doesn't exist in `test_versions` table

**Solution**:
1. Verify version exists: `SELECT * FROM test_versions WHERE version_code = 'MATH10_HK1_001'`
2. If not, upload test version first via `/teacher_quiz`

### Issue 3: Python Script Fails
**Error**: "Python script failed with exit code: 1"

**Cause**: Python error (missing dependencies, file not found, etc.)

**Solution**:
1. Check Python environment: `python --version`
2. Verify dependencies: `pip install -r requirements.txt`
3. Test script manually: `python py/2.py test_image.jpg 0.35`
4. Check script output for errors

### Issue 4: No Results Displayed
**Cause**: `published = false` in results table

**Solution**:
```sql
UPDATE results SET published = true WHERE result_id = ?;
```

---

## 📁 File Structure

```
DAT301mK/
├── src/main/
│   ├── java/com/example/dat301mk/
│   │   ├── controller/
│   │   │   ├── MainController.java          (scan_omr page)
│   │   │   └── OmrController.java           (API endpoint)
│   │   ├── service/
│   │   │   └── impl/
│   │   │       └── OmrGradingServiceImpl.java
│   │   ├── entity/
│   │   │   ├── Test.java
│   │   │   ├── TestVersion.java
│   │   │   ├── OmrSheet.java
│   │   │   ├── GradedItem.java
│   │   │   └── Result.java
│   │   └── repository/
│   │       ├── TestVersionRepository.java
│   │       ├── OmrSheetRepository.java
│   │       └── ResultRepository.java
│   └── resources/
│       ├── templates/
│       │   ├── Scan_omr.html
│       │   ├── student_home.html
│       │   └── teacher_quiz.html
│       └── application.properties
├── py/
│   ├── 2.py                                 (OMR processing script)
│   └── requirements.txt
└── uploads/omr/                             (Uploaded images)
```

---

## ✅ Testing Checklist

### Backend Tests
- [ ] POST `/api/omr/process` with valid image
- [ ] Verify omr_sheets record created
- [ ] Verify graded_items records created
- [ ] Verify results record created
- [ ] Check student_id maps to correct user

### Frontend Tests
- [ ] Upload image via Scan_omr.html
- [ ] Click "Chấm bài" button
- [ ] See results display
- [ ] View question breakdown
- [ ] Check output image preview

### Integration Tests
- [ ] Teacher uploads test version
- [ ] Scan student OMR sheet
- [ ] Verify score saved correctly
- [ ] Student sees score on student_home
- [ ] Score matches answer key

---

## 🎓 Sample Data

### Insert Test Student
```sql
INSERT INTO users (username, email, password, full_name, role)
VALUES ('student_test', 'test@example.com', 'pass123', 'Test Student', 'student');
-- This will auto-generate id_omr = '000001'
```

### Insert Test Version
```sql
INSERT INTO test_versions (test_id, version_code, pdf_path, csv_answer_path, uploaded_by)
VALUES (1, 'MATH10_HK1_001', '/files/test.pdf', '/files/answers.csv', 2);
```

### View Student Results
```sql
SELECT 
    u.id_omr AS StudentID,
    u.full_name AS StudentName,
    t.title AS TestName,
    tv.version_code AS TestVersion,
    r.total_score AS Score,
    r.total_correct AS Correct,
    r.total_wrong AS Wrong,
    r.graded_at AS GradedDate
FROM results r
JOIN users u ON r.student_id = u.user_id
JOIN tests t ON r.test_id = t.test_id
JOIN omr_sheets os ON r.sheet_id = os.sheet_id
JOIN test_versions tv ON os.version_id = tv.version_id
WHERE u.id_omr = '000001';
```

---

## 🔐 Security Considerations

1. **Role-based Access**:
   - Only teachers can access `/scan_omr`
   - Students redirected to `/student_home`

2. **File Validation**:
   - Validate uploaded images (JPEG/PNG only)
   - Check file size limits
   - Sanitize filenames

3. **Data Privacy**:
   - Students can only see their own results
   - Teachers can see all results for their classes

---

## 📞 Support

For issues or questions:
1. Check logs in console/terminal
2. Verify database connections
3. Test Python script independently
4. Review error stack traces

---

## 🎉 Success Criteria

The system is working correctly when:
- ✅ Teacher uploads test version successfully
- ✅ Scan_omr page loads without errors
- ✅ Image upload and preview works
- ✅ Python script processes OMR sheet
- ✅ Results save to database
- ✅ Student sees score on student_home
- ✅ Question breakdown displays correctly

---

*Last Updated: November 6, 2025*

