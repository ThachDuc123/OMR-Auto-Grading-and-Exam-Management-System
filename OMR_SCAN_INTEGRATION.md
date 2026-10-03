# 📊 OMR Scan Integration Guide

## 🎯 Overview

This system integrates OMR (Optical Mark Recognition) scanning functionality with Spring Boot backend to automatically grade multiple-choice tests and save results to the database.

## 🏗️ Architecture

```
┌─────────────────┐      ┌──────────────┐      ┌─────────────────┐
│  Scan_omr.html  │──────│ Python Flask │──────│ Spring Boot API │
│   (Frontend)    │      │   (2.py)     │      │  (/api/omr/*)   │
└─────────────────┘      └──────────────┘      └─────────────────┘
         │                                              │
         │                                              ▼
         │                                      ┌───────────────┐
         │                                      │   Database    │
         └──────────────────────────────────────│   (MySQL)     │
                 (Save results directly)        └───────────────┘
```

## 📋 Data Flow

### 1. User Uploads OMR Sheet
- User selects image file in `Scan_omr.html`
- Frontend displays preview

### 2. Python Processing (2.py)
- Image is sent to Flask API at `http://127.0.0.1:5000/api/omr/grade`
- Python script detects:
  - Student ID (6-digit `id_omr`)
  - Test version code (`mã đề`)
  - Marked answers (A, B, C, D)
- Compares with answer key from database
- Returns JSON with results

### 3. Frontend Receives Results
```json
{
  "success": true,
  "data": {
    "studentId": "000001",
    "testCode": "MATH10_HK1_001",
    "correctAnswers": 8,
    "totalQuestions": 10,
    "score": 8.0,
    "questions": [
      {
        "question": 1,
        "studentAnswer": "A",
        "correctAnswer": "A",
        "isCorrect": true
      },
      ...
    ],
    "outputImage": "base64_encoded_image"
  }
}
```

### 4. Save to Database
Frontend calls Spring Boot API: `POST /api/omr/save-result`

**Request Body:**
```json
{
  "sbd": "000001",
  "made": "MATH10_HK1_001",
  "imagePath": "/omr_sheets/1699200000.jpg",
  "score": 8.0,
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

**Response:**
```json
{
  "success": true,
  "message": "Result saved successfully",
  "resultId": 123,
  "studentName": "Ngô Minh Học",
  "score": 8.0,
  "maxScore": 10.0
}
```

## 🗄️ Database Schema

### Tables Involved

#### 1. `omr_sheets` (Bài làm)
- `sheet_id` (PK)
- `test_id` (FK → tests)
- `version_id` (FK → test_versions)
- `student_id` (FK → users)
- `image_path` - Path to scanned image
- `processed` - TRUE when graded
- `processing_time`

#### 2. `graded_items` (Chi tiết từng câu)
- `graded_item_id` (PK)
- `sheet_id` (FK → omr_sheets)
- `question_number` - Câu số mấy (1, 2, 3...)
- `detected_option` - Đáp án học sinh chọn (A/B/C/D)
- `correct_option` - Đáp án đúng
- `is_correct` - TRUE/FALSE
- `score_awarded` - Điểm được (0 hoặc 1)

#### 3. `results` (Tổng kết)
- `result_id` (PK)
- `sheet_id` (FK → omr_sheets) UNIQUE
- `student_id` (FK → users)
- `test_id` (FK → tests)
- `total_correct` - Số câu đúng
- `total_wrong` - Số câu sai
- `total_blank` - Số câu bỏ trống
- `total_score` - Tổng điểm
- `max_score` - Điểm tối đa
- `published` - TRUE để hiển thị cho học sinh

## 🔧 Backend Implementation

### OmrApiController
```java
@RestController
@RequestMapping("/api/omr")
@CrossOrigin(origins = {"http://localhost:8081", "http://127.0.0.1:5000"})
public class OmrApiController {
    
    @PostMapping("/save-result")
    public ResponseEntity<?> saveOmrResult(@RequestBody Map<String, Object> payload) {
        // 1. Extract data from payload
        String idOmr = (String) payload.get("sbd");
        String versionCode = (String) payload.get("made");
        Float score = parseScore(payload.get("score"));
        
        // 2. Find student by id_omr
        Users student = usersRepository.findByIdOmr(idOmr)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // 3. Find test version
        TestVersion version = testVersionRepository.findByVersionCode(versionCode)
            .orElseThrow(() -> new RuntimeException("Test version not found"));
        
        // 4. Save OmrSheet
        // 5. Save GradedItems
        // 6. Save Result
        // 7. Return response
    }
}
```

### OmrScanService
```java
@Service
public class OmrScanService {
    
    @Transactional
    public Result processScanResult(
        String idOmr,
        String versionCode,
        String imagePath,
        Map<Integer, String> answers,
        Map<Integer, String> correctAnswers,
        Float score
    ) {
        // Implementation in OmrScanService.java
    }
}
```

## 🖥️ Frontend Implementation

### Main Functions

#### 1. `submitForGrading()`
- Sends image to Python backend
- Receives grading results
- Calls `renderResults()` to display
- Calls `saveResultsToDatabase()` to persist

#### 2. `saveResultsToDatabase(payload)`
- Prepares data for Spring Boot API
- Sends POST request to `/api/omr/save-result`
- Shows success/error notification

#### 3. `renderResults(payload)`
- Displays summary card (SBD, mã đề, điểm)
- Shows detailed questions table
- Displays graded image preview

## 🚀 Setup Instructions

### 1. Database Setup
```sql
-- Run OMR.sql to create all tables
SOURCE D:\ki 7\DAT301mK\OMR.sql;

-- Insert test data
INSERT INTO users (username, email, password, full_name, role) VALUES
('student01', 'student01@example.com', 'stu123', 'Ngô Minh Học', 'student');
-- id_omr will be auto-generated as '000001'

INSERT INTO subjects (subject_code, subject_name) VALUES
('MATH10', 'Toán học lớp 10');

INSERT INTO tests (subject_id, test_code, title, total_questions, created_by) VALUES
(1, 'MATH10_HK1', 'Kiểm tra HK1 - Toán 10', 10, 2);

INSERT INTO test_versions (test_id, version_code, pdf_path, csv_answer_path, uploaded_by) VALUES
(1, 'MATH10_HK1_001', '/files/tests/MATH10_HK1_001.pdf', '/files/answers/MATH10_HK1_001.csv', 2);
```

### 2. Python Backend (Flask)
```bash
cd D:\ki 7\DAT301mK\py
pip install flask flask-cors opencv-python numpy ultralytics
python app.py
# Server runs on http://127.0.0.1:5000
```

### 3. Spring Boot Backend
```bash
cd D:\ki 7\DAT301mK
mvn spring-boot:run
# Server runs on http://localhost:8081
```

### 4. Access Frontend
- Open: `http://localhost:8081/scan_omr`
- Login as teacher or student
- Upload OMR sheet image
- Click "Chấm bài" to process

## 🔍 Testing Flow

### Test Data
- **Student ID (id_omr):** `000001`
- **Test Version:** `MATH10_HK1_001`
- **Answer Key:** Must be in database (`test_versions.csv_answer_path`)

### Step-by-Step Test
1. **Upload Image**
   - Select OMR sheet image
   - Ensure it has readable ID and version code

2. **Process**
   - Click "📝 Chấm bài"
   - Watch console logs:
     ```
     📤 POST http://127.0.0.1:5000/api/omr/grade
     ✅ Success data: {studentId: "000001", ...}
     💾 Saving results to database...
     📤 Sending to /api/omr/save-result
     ✅ Save successful
     ```

3. **Verify Results**
   - Check sidebar for summary
   - Check details section for question breakdown
   - Verify in database:
     ```sql
     SELECT * FROM results WHERE student_id = (SELECT user_id FROM users WHERE id_omr = '000001');
     SELECT * FROM graded_items WHERE sheet_id = (SELECT sheet_id FROM omr_sheets WHERE student_id = ...);
     ```

## 📊 Student View Results

Students can view their results on the dashboard (`student_home.html`):

```java
@GetMapping("/student_home")
public String student_home(Model model, Principal principal) {
    // Load results for logged-in student
    List<Result> results = resultRepository.findByStudent(currentUser);
    model.addAttribute("results", results);
    return "student_home";
}
```

## 🐛 Troubleshooting

### Error: Student not found
- **Cause:** `id_omr` in image doesn't match database
- **Fix:** Check `users` table: `SELECT user_id, id_omr FROM users WHERE role = 'student';`

### Error: Test version not found
- **Cause:** `version_code` (mã đề) doesn't exist
- **Fix:** Check `test_versions` table: `SELECT version_code FROM test_versions;`

### Error: CORS
- **Cause:** Frontend on different origin
- **Fix:** Verify `@CrossOrigin` annotation includes your origin

### Error: Python backend not responding
- **Cause:** Flask server not running
- **Fix:** 
  ```bash
  cd D:\ki 7\DAT301mK\py
  python app.py
  ```

## 📝 Sample Queries

### Get all results for a student
```sql
SELECT 
    r.result_id,
    u.full_name,
    u.id_omr,
    t.title AS test_title,
    tv.version_code,
    r.total_score,
    r.max_score,
    r.total_correct,
    r.total_wrong,
    r.graded_at
FROM results r
JOIN users u ON r.student_id = u.user_id
JOIN tests t ON r.test_id = t.test_id
JOIN omr_sheets o ON r.sheet_id = o.sheet_id
JOIN test_versions tv ON o.version_id = tv.version_id
WHERE u.id_omr = '000001';
```

### Get detailed answers for a result
```sql
SELECT 
    gi.question_number,
    gi.detected_option AS student_answer,
    gi.correct_option AS correct_answer,
    gi.is_correct,
    gi.score_awarded
FROM graded_items gi
JOIN omr_sheets o ON gi.sheet_id = o.sheet_id
JOIN users u ON o.student_id = u.user_id
WHERE u.id_omr = '000001'
  AND o.version_id = (SELECT version_id FROM test_versions WHERE version_code = 'MATH10_HK1_001')
ORDER BY gi.question_number;
```

## 🎨 UI Features

### Scan Page (`/scan_omr`)
- ✅ Upload image
- ✅ Adjust confidence threshold
- ✅ Process and grade
- ✅ View results summary
- ✅ View detailed questions
- ✅ See graded image with markings
- ✅ Auto-save to database

### Student Dashboard (`/student_home`)
- ✅ View all test results
- ✅ See score and statistics
- ✅ View detailed answers
- ✅ Filter by subject/class

## 🔐 Security

### Role-Based Access
- **Students:** Can only view their own results
- **Teachers:** Can view all results for their classes
- **Admin:** Can view all results

### Data Validation
- Verify student exists before saving
- Verify test version exists
- Prevent duplicate submissions (update if exists)

## 📈 Future Enhancements

- [ ] Batch upload multiple OMR sheets
- [ ] Export results to Excel
- [ ] Email notifications to students
- [ ] Analytics dashboard for teachers
- [ ] Mobile app for scanning

## 📞 Support

For issues or questions:
- Check logs: `target/logs/spring-boot-logger.log`
- Check Python logs: `py/app.log`
- Database logs: MySQL error log

---

**Version:** 1.0  
**Last Updated:** November 6, 2025  
**Author:** DAT301mK Team

