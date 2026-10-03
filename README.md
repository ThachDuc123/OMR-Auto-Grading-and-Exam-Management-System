# 📝 OMR Auto-Grading & Exam Management System

### A web-based examination management system with automated OMR scanning and grading

<p align="center">

**Create Exams → Take Tests → Scan OMR → Automatic Grading → Store Results → View Scores**

</p>

---

## 🚀 Overview

**OMR Auto-Grading & Exam Management System** is a web-based examination platform that combines a traditional exam-management system with **automatic Optical Mark Recognition (OMR)**.

Instead of manually checking every answer sheet, a teacher can scan or upload a student's completed OMR sheet and let the system automatically:

```text
OMR Sheet
    ↓
Image Processing
    ↓
Detect Student ID + Test Version
    ↓
Detect Selected Answers
    ↓
Compare with Answer Key
    ↓
Calculate Score
    ↓
Save Result
    ↓
Student Views Score
```

The system combines a **Spring Boot web application**, **MySQL database**, and a separate **Python OMR processing service**.

---

# 🎯 What Problem Does It Solve?

Traditional multiple-choice exams often require teachers to manually:

* Check answer sheets
* Identify student information
* Compare answers with the answer key
* Calculate scores
* Record results
* Publish results to students

This project automates that workflow.

A teacher only needs to upload the exam configuration and scan the completed OMR sheet.

The system handles the rest.

---

#  Key Features

##  Teacher

Teachers can manage the examination workflow:

* Create and manage tests
* Manage test versions
* Upload test documents
* Upload answer keys
* Scan student OMR sheets
* Automatically grade submitted sheets
* Review question-by-question results
* Store examination results

---

##  Student

Students can:

* Access their examination information
* View completed examination results
* View published scores
* Review their grading results

Student results are retrieved from the database after the teacher publishes them.

---

##  Administrator

The system includes role-based access for administrative operations.

Supported roles include:

```text
ADMIN
TEACHER
STUDENT
```

Each role has different permissions within the system.

---

#  Automatic OMR Grading

The most important part of the project is the automated OMR pipeline.

The system processes a scanned answer sheet and extracts:

```text
┌──────────────────────────────┐
│         OMR Sheet            │
├──────────────────────────────┤
│ Student ID                   │
│ Test Version                 │
│                              │
│ Q1   ○ A   ● B   ○ C   ○ D   │
│ Q2   ● A   ○ B   ○ C   ○ D   │
│ Q3   ○ A   ○ B   ● C   ○ D   │
│ ...                          │
└──────────────┬───────────────┘
               ↓
        OMR Processing
               ↓
        Detected Answers
               ↓
        Answer Key Comparison
               ↓
             Score
```

The current OMR integration detects:

* **6-digit student ID (`id_omr`)**
* **Test version code (`mã đề`)**
* **Marked answers**
* **Correct / incorrect answers**
* **Total score**

The Python processing layer uses **YOLO-based bubble detection** to identify marked regions on the answer sheet.

---

#  System Architecture

The project is split into a web application and an OMR processing component.

```text
                         ┌─────────────────────┐
                         │       Student       │
                         │                     │
                         │   View Exam Result  │
                         └──────────┬──────────┘
                                    │
                                    ↓
┌─────────────────┐       ┌─────────────────────┐
│     Teacher     │──────→│   Spring Boot App   │
│                 │       │                     │
│ • Create Exam   │       │ • Authentication    │
│ • Upload Key    │       │ • Exam Management   │
│ • Scan OMR      │       │ • Result Management │
└─────────────────┘       └──────────┬──────────┘
                                     │
                          ┌──────────┴──────────┐
                          ↓                     ↓
                  ┌──────────────┐      ┌───────────────┐
                  │    MySQL     │      │ Python / OMR  │
                  │   Database   │      │   Processor   │
                  └──────────────┘      └───────┬───────┘
                                                 │
                                                 ↓
                                           OMR Image
                                                 │
                                                 ↓
                                          YOLO Detection
                                                 │
                                                 ↓
                                          Grading Result
```

The repository documentation describes the OMR flow as:

**Scan page → Python processing → Spring Boot API → MySQL → result display.**

---

#  Complete Examination Workflow

## 1. Teacher Creates an Exam

The teacher creates an examination and its test version.

```text
Exam
 ↓
Test Version
 ↓
Answer Key
```

A test version stores information such as:

* Version code
* Exam reference
* PDF path
* Answer-key path

---

## 2. Student Takes the Exam

The student completes the printed multiple-choice answer sheet.

```text
Printed Exam
     +
OMR Answer Sheet
     ↓
Student fills answers
```

---

## 3. Teacher Scans the Answer Sheet

The teacher opens the OMR scanning interface and uploads the scanned image.

```text
Teacher
   ↓
Scan OMR
   ↓
Upload Image
   ↓
Start Grading
```

The frontend sends the image to the OMR processing backend.

---

#  OMR Processing Pipeline

The Python processing service receives the image and performs the recognition process.

```text
                OMR Image
                    ↓
            Image Processing
                    ↓
             YOLO Detection
                    ↓
       ┌────────────┴────────────┐
       ↓                         ↓
 Student ID                 Test Version
       │                         │
       └────────────┬────────────┘
                    ↓
             Detect Answers
                    ↓
             Compare Answer Key
                    ↓
              Calculate Score
                    ↓
              Return JSON
```

The processing service returns structured data containing information such as:

```json
{
  "sbd": "000123",
  "made": "MATH10_HK1_001",
  "total_questions": 20,
  "correct": 15,
  "wrong": 5,
  "blank": 0,
  "score": 7.5
}
```

The backend then persists the result.

---

#  Result Storage

The system stores both the overall score and the detailed result for each question.

### `omr_sheets`

Stores information about the submitted OMR sheet.

```text
sheet_id
test_id
version_id
student_id
image_path
processed
processing_time
```

### `graded_items`

Stores question-level grading information.

```text
question_number
detected_option
correct_option
is_correct
score_awarded
```

### `results`

Stores the final examination result.

```text
student_id
test_id
total_correct
total_wrong
total_blank
total_score
max_score
published
```

This separation allows the system to retain both:

**overall score**

and

**question-by-question grading details**.

The database structure and OMR result flow are documented in the repository's integration guide.

---

#  Authentication & Authorization

The application uses **Spring Security** for authentication and authorization.

The project also includes **OAuth2 client support**, allowing external OAuth-based authentication to be integrated into the application.

The application separates users into:

```text
              ┌──────────────┐
              │     User     │
              └───────┬──────┘
                      │
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
      ADMIN         TEACHER       STUDENT
```

This allows the same platform to provide different workflows depending on the user's role.

---

#  Real-Time Communication

The backend includes **WebSocket support**.

This provides a foundation for real-time communication between the web interface and backend services. The Spring Boot project configuration includes the WebSocket starter alongside the main web, security, JPA and Thymeleaf components.

---

#  Technology Stack

| Layer                 | Technology                  |
| --------------------- | --------------------------- |
| Backend               | Spring Boot 3.5.6           |
| Language              | Java 17                     |
| Web                   | Spring MVC                  |
| Template Engine       | Thymeleaf                   |
| ORM                   | Spring Data JPA / Hibernate |
| Database              | MySQL                       |
| Security              | Spring Security             |
| Authentication        | OAuth2 Client               |
| Real-time             | WebSocket                   |
| OMR Processing        | Python                      |
| OMR Detection         | YOLO                        |
| Build Tool            | Maven                       |
| Code Mapping          | MapStruct                   |
| Boilerplate Reduction | Lombok                      |

The versions and dependencies are defined in the project's `pom.xml`.

---

#  Project Structure

```text
DAT301/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   │
│   │   └── resources/
│   │       ├── templates/
│   │       ├── static/
│   │       └── application.properties
│   │
│   └── test/
│
├── runs/
│   └── predict/
│       └── omr_sample/
│
├── uploads/
│   └── omr/
│
├── OMR.sql
├── OMR_SCAN_INTEGRATION.md
├── OMR_SYSTEM_COMPLETE_GUIDE.md
├── FIX_OMR_404_ERROR.md
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

#  Database Design

The examination system is built around several related entities.

```text
Users
  │
  ├───────────────┐
  │               │
  ↓               ↓
Tests        Student Results
  │
  ↓
Test Versions
  │
  ↓
OMR Sheets
  │
  ↓
Graded Items
  │
  ↓
Results
```

The database schema is included in:

```text
OMR.sql
```

The repository documentation also describes the relationships between users, tests, test versions, OMR sheets, graded items and results.

---

#  Getting Started

## Requirements

Before running the project, prepare:

* **JDK 17**
* **Maven** or the included Maven Wrapper
* **MySQL**
* **Python**
* OMR processing dependencies
* A configured database

The Spring Boot project is configured for Java 17 and Spring Boot 3.5.6.

---

## 1. Clone the repository

```bash
git clone https://github.com/ThachDuc123/DAT301.git

cd DAT301
```

---

## 2. Configure MySQL

Create the required database and import:

```text
OMR.sql
```

Then configure the database connection in:

```text
src/main/resources/application.properties
```

---

## 3. Configure Python OMR Processing

The OMR system expects the Python processing component to be available.

The documented configuration includes:

```properties
omr.upload.dir=uploads/omr
python.script.path=py/2.py
python.executable=python
```

The Python script processes the uploaded OMR image and returns the grading result to the Java application.

---

## 4. Run Spring Boot

Using Maven:

```bash
mvn spring-boot:run
```

Or on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

---

#  OMR Example Flow

A typical grading session looks like:

```text
Teacher
   │
   │ Upload OMR image
   ↓
Spring Boot
   │
   │ Send image
   ↓
Python OMR Processor
   │
   │ Detect ID / Test Code / Answers
   ↓
Grading Result
   │
   │ JSON
   ↓
Spring Boot
   │
   ├── Save OMR sheet
   ├── Save question results
   └── Save final score
          │
          ↓
       MySQL
          │
          ↓
      Student
          │
          ↓
     View Result
```

---

#  Example Result

The system can return a detailed result containing:

```text
Student ID:       000001
Test Version:     MATH10_HK1_001

Correct Answers:  8
Wrong Answers:    2
Blank Answers:    0

Score:            8.0 / 10
```

The result can also contain question-level information:

```text
Question 1 → A → Correct
Question 2 → C → Wrong
Question 3 → B → Correct
...
```

This allows both the total score and individual answers to be inspected.

---

#  Why OMR?

The main idea of the project is to combine a conventional examination-management platform with automated document recognition.

Instead of:

```text
Teacher
   ↓
Read answer
   ↓
Compare answer
   ↓
Calculate score
   ↓
Enter result
```

the system moves toward:

```text
Teacher
   ↓
Scan OMR
   ↓
Automatic recognition
   ↓
Automatic grading
   ↓
Database
   ↓
Published result
```

This makes OMR the bridge between **physical paper examinations** and a **digital examination management system**.

---

#  Documentation

The repository contains additional technical documentation:

### OMR System Guide

[`OMR_SYSTEM_COMPLETE_GUIDE.md`](OMR_SYSTEM_COMPLETE_GUIDE.md)

Detailed explanation of the complete OMR workflow.

### OMR Integration

[`OMR_SCAN_INTEGRATION.md`](OMR_SCAN_INTEGRATION.md)

Documents communication between the frontend, Python OMR processor, Spring Boot API and MySQL database.

### Database

[`OMR.sql`](OMR.sql)

Database schema and related SQL scripts.

---

#  Project Highlights

This project combines several different software-engineering areas in a single system:

```text
        Web Application
              │
     ┌────────┼────────┐
     ↓        ↓        ↓
 Security   Database   UI
     │        │        │
     └────────┼────────┘
              ↓
          Exam System
              │
              ↓
        OMR Integration
              │
              ↓
       Computer Vision
              │
              ↓
          Auto-Grading
```

The interesting part is not only the individual technologies, but how they communicate as one workflow.

---

#  Current Scope

### Implemented

* ✅ Role-based examination system
* ✅ Student / Teacher / Admin roles
* ✅ Test management
* ✅ Test versions
* ✅ Answer-key management
* ✅ OMR image upload
* ✅ Automatic OMR processing
* ✅ Student ID recognition
* ✅ Test version recognition
* ✅ Answer recognition
* ✅ Automatic score calculation
* ✅ Question-level grading
* ✅ MySQL result persistence
* ✅ Student result viewing
* ✅ Spring Security
* ✅ OAuth2 client support
* ✅ WebSocket support

---

#  Possible Future Improvements

Potential extensions include:

* Better OMR recognition robustness
* Batch processing of multiple answer sheets
* Processing-time optimization
* More detailed teacher analytics
* Exam statistics dashboards
* Exporting results to Excel/PDF
* More advanced recognition confidence handling
* Automated result notifications
* Deployment as a production web service

---

#  Project

**OMR Auto-Grading & Exam Management System**

A full-stack examination management platform combining:

**Spring Boot + MySQL + Thymeleaf + Python + YOLO + OMR**

The project demonstrates how a physical paper-based examination workflow can be connected to a digital platform through automated document recognition.

---

<p align="center">

###  Paper Exam → 🤖 OMR Recognition → 📊 Automatic Grading → 💾 Digital Results

</p>
